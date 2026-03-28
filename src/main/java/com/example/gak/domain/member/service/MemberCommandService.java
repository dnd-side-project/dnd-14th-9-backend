package com.example.gak.domain.member.service;

import static com.example.gak.global.security.oauth2.CustomOAuth2AuthorizedClientService.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.gak.domain.chat.repository.ChatMessageRepository;
import com.example.gak.domain.member.converter.MemberConverter;
import com.example.gak.domain.member.dto.MemberRequestDTO;
import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
import com.example.gak.domain.record.entity.Record;
import com.example.gak.domain.record.repository.RecordRepository;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.session.repository.SessionRoomMemberRepository;
import com.example.gak.domain.session.repository.SessionRoomRepository;
import com.example.gak.domain.task.entity.Task;
import com.example.gak.domain.task.repository.SubTaskRepository;
import com.example.gak.domain.task.repository.TaskRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.aws.AmazonS3Manager;
import com.example.gak.global.security.oauth2.dto.OAuth2MemberDto;
import com.example.gak.global.security.oauth2.external.GoogleClient;
import com.example.gak.global.security.oauth2.external.KakaoClient;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService {

	private final MemberRepository memberRepository;
	private final AmazonS3Manager amazonS3Manager;
	private final KakaoClient kakaoClient;
	private final GoogleClient googleClient;
	private final StringRedisTemplate stringRedisTemplate;

	private final RecordRepository recordRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final TaskRepository taskRepository;
	private final SubTaskRepository subTaskRepository;
	private final SessionRoomRepository sessionRoomRepository;
	private final SessionRoomMemberRepository sessionRoomMemberRepository;

	public Long synchronize(OAuth2MemberDto oAuth2MemberDto) {
		Optional<Member> optionalMember = memberRepository.findBySocialProviderAndProviderId(
			oAuth2MemberDto.getProvider(),
			oAuth2MemberDto.getProviderId()
		);

		if (optionalMember.isEmpty()) {
			Member member = new Member(
				oAuth2MemberDto.getNickname(),
				oAuth2MemberDto.getProfileImage().orElse(""), // 기본 이미지 디자인 완성 시 URL 추가
				oAuth2MemberDto.getEmail().orElse(null),
				null,
				null,
				null,
				null,
				oAuth2MemberDto.getProvider(),
				oAuth2MemberDto.getProviderId()
			);
			Member persisted = memberRepository.save(member);

			Record record = new Record(persisted);
			recordRepository.save(record);

			return persisted.getId();
		}

		Member member = optionalMember.get();
		if (member.isDeleted()) {
			member.activate();

			Record record = new Record(member);
			recordRepository.save(record);
		}

		return member.getId();
	}

	public void markFirstLoginComplete(Long memberId) {
		Member member = findMember(memberId);

		if (member.isFirstLogin()) {
			member.markLoginDone();
		}
	}

	public MemberResponseDTO.UpdateMemberResponseDTO updateMember(
		Long memberId,
		MemberRequestDTO.UpdateMemberRequestDTO request
	) {
		Member member = findMember(memberId);

		member.updateNickname(request.getNickname());
		member.updateBio(request.getBio());
		member.updateInterestCategories(
			request.getFirstInterestCategory(),
			request.getSecondInterestCategory(),
			request.getThirdInterestCategory()
		);

		return MemberConverter.toUpdateMemberResponseDTO(member);
	}

	public MemberResponseDTO.UpdateMemberResponseDTO updateProfileImage(Long memberId, MultipartFile newProfileImage) {
		Member member = findMember(memberId);

		if (newProfileImage != null) {
			String newProfileImageUrl = amazonS3Manager.uploadFile(
				amazonS3Manager.generateProfileKeyName(),
				newProfileImage
			);

			String profileImageUrl = member.getProfileImageUrl();
			if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
				amazonS3Manager.deleteFile(profileImageUrl);
			}
			member.updateProfileImageUrl(newProfileImageUrl);
		}

		return MemberConverter.toUpdateMemberResponseDTO(member);
	}

	public MemberResponseDTO.UpdateMemberResponseDTO updateNickname(
		Long memberId,
		MemberRequestDTO.UpdateMemberNicknameRequestDTO request
	) {
		Member member = findMember(memberId);
		member.updateNickname(request.getNickname());

		return MemberConverter.toUpdateMemberResponseDTO(member);
	}

	public MemberResponseDTO.UpdateMemberResponseDTO updateEmail(
		Long memberId,
		MemberRequestDTO.UpdateMemberEmailRequestDTO request
	) {
		Member member = findMember(memberId);

		if (request != null) {
			member.updateEmail(request.getEmail());
		}

		return MemberConverter.toUpdateMemberResponseDTO(member);
	}

	public MemberResponseDTO.UpdateMemberResponseDTO updateInterestCategories(
		Long memberId,
		MemberRequestDTO.UpdateMemberInterestCategoriesRequestDTO request
	) {
		Member member = findMember(memberId);

		if (request != null) {
			member.updateInterestCategories(
				request.getFirstInterestCategory(),
				request.getSecondInterestCategory(),
				request.getThirdInterestCategory()
			);
		}

		return MemberConverter.toUpdateMemberResponseDTO(member);
	}

	public void deleteProfileImage(Long memberId) {
		Member member = findMember(memberId);

		String profileImageUrl = member.getProfileImageUrl();
		if (profileImageUrl == null || profileImageUrl.isEmpty()) {
			return;
		}

		amazonS3Manager.deleteFile(profileImageUrl);
		member.deleteProfileImageUrl();
	}

	public void deleteMember(Long memberId) {
		if (sessionRoomRepository.existsByMemberIdAndStatusNot(memberId, SessionRoomStatus.COMPLETED)) {
			throw new GeneralException(GeneralErrorCode.MEMBER_HAS_ACTIVE_SESSION);
		}

		Member member = findMember(memberId);

		recordRepository.deleteByMemberId(memberId);
		chatMessageRepository.deleteByMemberId(memberId);
		sessionRoomMemberRepository.deleteByMemberId(memberId);

		List<Task> tasks = taskRepository.findByMemberId(memberId);
		if (!tasks.isEmpty()) {
			List<Long> taskIds = tasks.stream()
				.map(Task::getId)
				.toList();
			subTaskRepository.deleteByTaskIdIn(taskIds);
			taskRepository.deleteAllInBatch(tasks);
		}

		sessionRoomRepository.deleteByMemberId(memberId);

		member.delete();
		if (member.getSocialProvider().equals("kakao")) {
			kakaoClient.unlink(member.getProviderId());
		}
		if (member.getSocialProvider().equals("google")) {
			String googleRefreshKey = OAUTH2_REFRESH_TOKEN_KEY_PREFIX + member.getId();
			String googleRefreshToken = stringRedisTemplate.opsForValue().get(googleRefreshKey);

			String googleAccessKey = OAUTH2_ACCESS_TOKEN_KEY_PREFIX + member.getId();
			String googleAccessToken = stringRedisTemplate.opsForValue().get(googleAccessKey);

			if (googleAccessToken == null) {
				googleAccessToken = googleClient.reissueToken(googleRefreshToken).getAccess_token();
			}

			googleClient.unlink(googleAccessToken);
		}
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));
	}
}
