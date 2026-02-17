package com.example.gak.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "회원 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberQueryService memberQueryService;
	private final MemberCommandService memberCommandService;

	@Operation(summary = "내 프로필 조회 API")
	@GetMapping("/me/profile")
	public ApiResponse<MemberResponseDTO.GetProfileResponseDTO> getProfile(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		Long memberId = oAuth2User.getMemberId();
		MemberResponseDTO.GetProfileResponseDTO response = memberQueryService.getProfile(memberId);
		memberCommandService.markFirstLoginComplete(memberId);

		return ApiResponse.onSuccess(response);
	}

	@Operation(summary = "내 정보 조회 API (수정용)")
	@GetMapping("/me/edit")
	public ApiResponse<MemberResponseDTO.GetMemberResponseDTO> getMember(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		Long memberId = oAuth2User.getMemberId();
		MemberResponseDTO.GetMemberResponseDTO response = memberQueryService.getMember(memberId);

		return ApiResponse.onSuccess(response);
	}

	@Operation(summary = "내 프로필 이미지 수정 API")
	@PatchMapping("/me/profile-image")
	public ApiResponse<MemberResponseDTO.UpdateMemberResponseDTO> updateProfileImage(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		return ApiResponse.onSuccess(
			memberCommandService.updateProfileImage(oAuth2User.getMemberId(), profileImage)
		);
	}

	@Operation(summary = "내 닉네임 수정 API")
	@PatchMapping("/me/nickname")
	public ApiResponse<MemberResponseDTO.UpdateMemberResponseDTO> updateNickname(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@Valid @RequestBody MemberRequestDTO.UpdateMemberNicknameRequestDTO request
	) {
		return ApiResponse.onSuccess(
			memberCommandService.updateNickname(oAuth2User.getMemberId(), request)
		);
	}

	@Operation(summary = "내 이메일 수정 API")
	@PatchMapping("/me/email")
	public ApiResponse<MemberResponseDTO.UpdateMemberResponseDTO> updateEmail(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@Valid @RequestBody(required = false) MemberRequestDTO.UpdateMemberEmailRequestDTO request
	) {
		return ApiResponse.onSuccess(
			memberCommandService.updateEmail(oAuth2User.getMemberId(), request)
		);
	}

	@Operation(summary = "내 관심 카테고리 수정 API")
	@PatchMapping("/me/interest-categories")
	public ApiResponse<MemberResponseDTO.UpdateMemberResponseDTO> updateInterestCategories(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@RequestBody(required = false) MemberRequestDTO.UpdateMemberInterestCategoriesRequestDTO request
	) {
		return ApiResponse.onSuccess(
			memberCommandService.updateInterestCategories(oAuth2User.getMemberId(), request)
		);
	}

	@Operation(summary = "회원 탈퇴 API")
	@DeleteMapping("/me")
	public ApiResponse<Void> deleteMember(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
		memberCommandService.deleteMember(oAuth2User.getMemberId());

		return ApiResponse.onSuccess(null);
	}
}
