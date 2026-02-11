package com.example.gak.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService {

	private final MemberRepository memberRepository;

	public MemberResponseDTO getMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));

		return MemberResponseDTO.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getProfileImageUrl())
			.bio(member.getBio())
			.firstInterestCategory(member.getFirstInterestCategory())
			.secondInterestCategory(member.getSecondInterestCategory())
			.thirdInterestCategory(member.getThirdInterestCategory())
			.firstLogin(member.isFirstLogin())
			.build();
	}
}
