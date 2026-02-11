package com.example.gak.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.gak.domain.member.converter.MemberConverter;
import com.example.gak.domain.member.dto.MemberRequestDTO;
import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
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

	public Long synchronize(OAuth2MemberDto oAuth2MemberDto) {
		return memberRepository.findBySocialProviderAndProviderId(
				oAuth2MemberDto.getProvider(),
				oAuth2MemberDto.getProviderId()
			)
			.map(Member::getId)
			.orElseGet(() -> {
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
					return memberRepository.save(member).getId();
				}
			);
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

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));
	}
}
