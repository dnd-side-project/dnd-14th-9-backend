package com.example.gak.global.security.oauth2;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.example.gak.global.apiPayload.code.GeneralErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException exception
	) throws IOException {
		GeneralErrorCode errorCode = resolveErrorCode(exception);

		log.warn("OAuth2 login failure - message: {}",
			errorCode.getMessage(),
			exception
		);
		
		String registrationId = extractRegistrationId(request);
		String redirectUrl = String.format(
			"http://localhost:3000/api/auth/callback/%s?error=%s",
			registrationId,
			errorCode.getCode()
		);
		response.sendRedirect(redirectUrl);
	}

	private GeneralErrorCode resolveErrorCode(AuthenticationException exception) {
		if (!(exception instanceof OAuth2AuthenticationException e)) {
			return GeneralErrorCode.OAUTH2_UNKNOWN_ERROR;
		}

		String code = e.getError().getErrorCode();
		try {
			return GeneralErrorCode.valueOf(code);
		} catch (IllegalArgumentException ignored) {
		}

		return switch (code) {
			case "access_denied" -> GeneralErrorCode.OAUTH2_ACCESS_DENIED;
			case "invalid_request",
				 "invalid_scope",
				 "invalid_grant" -> GeneralErrorCode.OAUTH2_INVALID_REQUEST;
			case "server_error",
				 "temporarily_unavailable",
				 "invalid_client",
				 "unauthorized_client",
				 "unsupported_response_type" -> GeneralErrorCode.OAUTH2_PROVIDER_ERROR;
			default -> GeneralErrorCode.OAUTH2_LOGIN_FAILED;
		};
	}

	private String extractRegistrationId(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String[] parts = uri.split("/");
		if (parts.length > 0) {
			return parts[parts.length - 1];
		}
		return "unknown";
	}
}
