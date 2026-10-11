package com.example.gak.global.security.oauth2;

import java.net.URI;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
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
	private final StringRedisTemplate stringRedisTemplate;

	public CustomAuthorizationRequestResolver(
		ClientRegistrationRepository repository,
		StringRedisTemplate stringRedisTemplate
	) {
		this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
			repository, "/oauth2/authorization"
		);
		this.stringRedisTemplate = stringRedisTemplate;
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

		String codeChallenge = request.getParameter("codeChallenge");

		String referer = request.getHeader(HttpHeaders.REFERER);
		if (referer != null) {
			String origin = URI.create(referer).getScheme() + "://" + URI.create(referer).getAuthority();
			String newState = origin + "|" + authRequest.getState();
			authRequest = OAuth2AuthorizationRequest.from(authRequest)
				.state(newState)
				.build();
		}

		if (codeChallenge != null) {
			stringRedisTemplate.opsForValue().set(
				"oauth:challenge:" + authRequest.getState(),
				codeChallenge,
				Duration.ofMinutes(1)
			);
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
