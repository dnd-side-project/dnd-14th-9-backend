package com.example.gak.global.security.oauth2;

import java.io.IOException;
import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.gak.global.security.jwt.JwtProvider;
import com.example.gak.global.security.jwt.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtProvider jwtProvider;
	private final JwtService jwtService;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException {
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String registrationId = oauthToken.getAuthorizedClientRegistrationId();

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();

		Long memberId = Long.parseLong(oAuth2User.getName());
		Instant now = Instant.now();
		String accessToken = jwtProvider.createAccessToken(memberId, now);
		String refreshToken = jwtProvider.createRefreshToken(memberId, now);

		String state = request.getParameter("state");
		String origin = null;
		if (state != null && state.contains("|")) {
			origin = state.split("\\|")[0];
		}

		String redirectUrl = UriComponentsBuilder
			.fromUriString(origin + "/api/auth/callback/" + registrationId)
			.queryParam("accessToken", accessToken)
			.queryParam("refreshToken", refreshToken)
			.build()
			.toUriString();

		jwtService.saveRefreshToken(memberId, refreshToken, now);

		/*response.addHeader(
			HttpHeaders.SET_COOKIE,
			AuthCookieProvider.createAccessTokenCookie(accessToken).toString()
		);
		response.addHeader(
			HttpHeaders.SET_COOKIE,
			AuthCookieProvider.createRefreshTokenCookie(refreshToken).toString()
		);*/
		response.sendRedirect(redirectUrl);
	}
}
