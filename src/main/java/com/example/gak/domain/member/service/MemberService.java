package com.example.gak.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
import com.example.gak.global.security.oauth2.dto.OAuth2MemberDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	public Long synchronize(OAuth2MemberDto oAuth2MemberDto) {
		return memberRepository.findByProviderId(oAuth2MemberDto.getProviderId())
			.map(Member::getId)
			.orElseGet(() -> {
					Member member = new Member(
						oAuth2MemberDto.getNickname(),
						oAuth2MemberDto.getProfileImage().orElse(""), // 기본 이미지 디자인 완성 시 URL 추가
						null,
						null,
						oAuth2MemberDto.getProvider(),
						oAuth2MemberDto.getProviderId()
					);
					return memberRepository.save(member).getId();
				}
			);
	}
}
