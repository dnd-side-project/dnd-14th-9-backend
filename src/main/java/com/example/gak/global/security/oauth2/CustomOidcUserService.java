package com.example.gak.global.security.oauth2;

import java.util.Map;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.example.gak.domain.member.service.MemberCommandService;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.security.oauth2.dto.GoogleMemberDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

	private final MemberCommandService memberCommandService;

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		if (registrationId.equals("google")) {
			GoogleMemberDto googleMemberResponse = new GoogleMemberDto(registrationId, attributes);
			Long memberId = memberCommandService.synchronize(googleMemberResponse);
			return new CustomOidcUser(registrationId, memberId);
		}

		throw new GeneralException(GeneralErrorCode.UNSUPPORTED_PROVIDER);
	}
}
