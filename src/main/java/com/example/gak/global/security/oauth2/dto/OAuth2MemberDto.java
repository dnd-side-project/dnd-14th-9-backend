package com.example.gak.global.security.oauth2.dto;

import java.util.Optional;

public interface OAuth2MemberDto {

	String getProvider();

	String getProviderId();

	String getNickname();

	Optional<String> getProfileImage();
}
