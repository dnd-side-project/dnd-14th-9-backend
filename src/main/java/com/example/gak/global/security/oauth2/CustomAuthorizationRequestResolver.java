package com.example.gak.global.security.oauth2;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

	private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

	public CustomAuthorizationRequestResolver(ClientRegistrationRepository repository) {
		this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
			repository, "/oauth2/authorization"
		);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);

		return customize(authRequest, request);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);

		return customize(authRequest, request);
	}

	private OAuth2AuthorizationRequest customize(
		OAuth2AuthorizationRequest authRequest,
		HttpServletRequest request
	) {
		if (authRequest == null) {
			return null;
		}

		String referer = request.getHeader(HttpHeaders.REFERER);
		if (referer != null) {
			String origin = URI.create(referer).getScheme() + "://" + URI.create(referer).getAuthority();
			String newState = origin + "|" + authRequest.getState();
			authRequest = OAuth2AuthorizationRequest.from(authRequest)
				.state(newState)
				.build();
		}

		Object registrationId = authRequest.getAttribute("registration_id");
		if ("google".equals(registrationId)) {
			authRequest = OAuth2AuthorizationRequest.from(authRequest)
				.additionalParameters(params -> {
					params.put("prompt", "consent");
					params.put("access_type", "offline");
				})
				.build();
		}

		return authRequest;
	}
}
