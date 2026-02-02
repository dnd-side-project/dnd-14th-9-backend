package com.example.gak.global.security.oauth2.dto;

import java.util.Map;
import java.util.Optional;

public class KakaoMemberDto implements OAuth2MemberDto {

	private final String registrationId;
	private final Map<String, Object> attributes;
	private final Map<String, Object> kakaoAccount;
	private final Map<String, String> profile;

	public KakaoMemberDto(String registrationId, Map<String, Object> attributes) {
		this.registrationId = registrationId;
		this.attributes = attributes;
		this.kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
		this.profile = (Map<String, String>)kakaoAccount.get("profile");
	}

	@Override
	public String getProvider() {
		return registrationId;
	}

	@Override
	public String getProviderId() {
		return attributes.get("id").toString();
	}

	@Override
	public String getNickname() {
		return profile.get("nickname");
	}

	@Override
	public Optional<String> getProfileImage() {
		return Optional.ofNullable(profile.get("profile_image_url"));
	}
}
