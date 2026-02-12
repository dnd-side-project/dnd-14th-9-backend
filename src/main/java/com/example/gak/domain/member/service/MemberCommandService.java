package com.example.gak.domain.member.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.gak.domain.chatmessage.repository.ChatMessageRepository;
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
import com.example.gak.domain.subtask.repository.SubTaskRepository;
import com.example.gak.domain.task.entity.Task;
import com.example.gak.domain.task.repository.TaskRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.aws.AmazonS3Manager;
import com.example.gak.global.security.oauth2.dto.OAuth2MemberDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService {

	private final MemberRepository memberRepository;
	private final AmazonS3Manager amazonS3Manager;

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
		}

		return member.getId();
	}

	public void markFirstLoginComplete(Long memberId) {
		Member member = getMember(memberId);

		if (member.isFirstLogin()) {
			member.markLoginDone();
		}
	}

	public MemberResponseDTO.UpdateMemberResponseDTO updateProfileImage(Long memberId, MultipartFile newProfileImage) {
		Member member = getMember(memberId);

		if (newProfileImage == null) {
			return MemberConverter.toUpdateMemberResponseDTO(member);
		}

		String newProfileImageUrl = amazonS3Manager.uploadFile(
			amazonS3Manager.generateProfileKeyName(),
			newProfileImage
		);

		String profileImageUrl = member.getProfileImageUrl();
		if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
			amazonS3Manager.deleteFile(profileImageUrl);
		}
		member.updateProfileImageUrl(newProfileImageUrl);

		return MemberConverter.toUpdateMemberResponseDTO(member);
	}

	public MemberResponseDTO.UpdateMemberResponseDTO updateNickname(
		Long memberId,
		MemberRequestDTO.UpdateMemberNicknameRequestDTO request
	) {
		Member member = getMember(memberId);
		member.updateNickname(request.getNickname());

		return MemberConverter.toUpdateMemberResponseDTO(member);
	}

	public MemberResponseDTO.UpdateMemberResponseDTO updateInterestCategories(
		Long memberId,
		MemberRequestDTO.UpdateMemberInterestCategoriesRequestDTO request
	) {
		Member member = getMember(memberId);
		member.updateInterestCategories(
			request.getFirstInterestCategory(),
			request.getSecondInterestCategory(),
			request.getThirdInterestCategory()
		);

		return MemberConverter.toUpdateMemberResponseDTO(member);
	}

	public void deleteMember(Long memberId) {
		if (sessionRoomRepository.existsByMemberIdAndStatusNot(memberId, SessionRoomStatus.COMPLETED)) {
			throw new GeneralException(GeneralErrorCode.HAS_ACTIVE_SESSION);
		}

		Member member = getMember(memberId);

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
	}

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));
	}
}
