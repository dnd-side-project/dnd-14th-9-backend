package com.example.gak.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gak.domain.member.dto.MemberRequestDTO;
import com.example.gak.domain.member.dto.MemberResponseDTO;
import com.example.gak.domain.member.service.MemberCommandService;
import com.example.gak.domain.member.service.MemberQueryService;
import com.example.gak.global.apiPayload.ApiResponse;
import com.example.gak.global.security.oauth2.CustomOAuth2User;

import jakarta.validation.Valid;
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

	@PatchMapping("/me/profile-image")
	public ApiResponse<MemberResponseDTO.UpdateMemberResponseDTO> updateProfileImage(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		return ApiResponse.onSuccess(
			memberCommandService.updateProfileImage(oAuth2User.getMemberId(), profileImage)
		);
	}

	@PatchMapping("/me/nickname")
	public ApiResponse<MemberResponseDTO.UpdateMemberResponseDTO> updateNickname(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@Valid @RequestBody MemberRequestDTO.UpdateMemberNicknameRequestDTO request
	) {
		return ApiResponse.onSuccess(
			memberCommandService.updateNickname(oAuth2User.getMemberId(), request)
		);
	}

	@PatchMapping("/me/interest-categories")
	public ApiResponse<MemberResponseDTO.UpdateMemberResponseDTO> updateInterestCategories(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@RequestBody MemberRequestDTO.UpdateMemberInterestCategoriesRequestDTO request
	) {
		return ApiResponse.onSuccess(
			memberCommandService.updateInterestCategories(oAuth2User.getMemberId(), request)
		);
	}
}
