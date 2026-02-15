package com.example.gak.global.security.oauth2.dto;

import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleMemberDto implements OAuth2MemberDto {

	private final String registrationId;
	private final Map<String, Object> attributes;

	@Override
	public String getProvider() {
		return registrationId;
	}

	@Override
	public String getProviderId() {
		return attributes.get("sub").toString();
	}

	@Override
	public String getNickname() {
		return attributes.get("name").toString();
	}

	@Override
	public Optional<String> getProfileImage() {
		return Optional.ofNullable(attributes.get("picture")).map(Object::toString);
	}

	@Override
	public Optional<String> getEmail() {
		return Optional.ofNullable(attributes.get("email").toString());
	}
}
