package com.example.gak.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.service.MemberQueryService;
import com.example.gak.global.apiPayload.ApiResponse;
import com.example.gak.global.security.oauth2.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberQueryService memberQueryService;

	@GetMapping("/me")
	public ApiResponse<MemberResponseDTO> getMember(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
		return ApiResponse.onSuccess(
			memberQueryService.getMember(oAuth2User.getMemberId())
		);
	}
}
