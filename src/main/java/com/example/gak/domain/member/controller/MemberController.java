package com.example.gak.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.service.MemberCommandService;
import com.example.gak.domain.member.service.MemberQueryService;
import com.example.gak.global.apiPayload.ApiResponse;
import com.example.gak.global.security.oauth2.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberQueryService memberQueryService;
	private final MemberCommandService memberCommandService;

	@GetMapping("/me")
	public ApiResponse<MemberResponseDTO.GetMemberResponseDTO> getMember(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		Long memberId = oAuth2User.getMemberId();
		MemberResponseDTO.GetMemberResponseDTO response = memberQueryService.getMember(memberId);
		memberCommandService.markFirstLoginComplete(memberId);

		return ApiResponse.onSuccess(response);
	}
}
