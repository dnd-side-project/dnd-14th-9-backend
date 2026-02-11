package com.example.gak.global.security.oauth2;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.example.gak.domain.member.service.MemberCommandService;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.security.oauth2.dto.KakaoMemberDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberCommandService memberCommandService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		if (registrationId.equals("kakao")) {
			KakaoMemberDto kakaoMemberResponse = new KakaoMemberDto(registrationId, attributes);
			Long memberId = memberCommandService.synchronize(kakaoMemberResponse);
			return new CustomOAuth2User(registrationId, memberId);
		}

		throw new GeneralException(GeneralErrorCode.UNSUPPORTED_PROVIDER);
	}
}
