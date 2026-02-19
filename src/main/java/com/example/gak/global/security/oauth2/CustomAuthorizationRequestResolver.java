package com.example.gak.global.security.oauth2;

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
		if (authRequest == null) {
			return null;
		}

		String origin = request.getHeader(HttpHeaders.ORIGIN);
		if (origin != null) {
			request.getSession().setAttribute("CLIENT_ORIGIN", origin);
		}
		return authRequest;
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
		if (authRequest == null) {
			return null;
		}

		String origin = request.getHeader(HttpHeaders.ORIGIN);
		if (origin != null) {
			request.getSession().setAttribute("CLIENT_ORIGIN", origin);
		}
		return authRequest;
	}
}
