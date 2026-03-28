package com.example.gak.global.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.gak.global.security.oauth2.CustomAuthorizationRequestResolver;
import com.example.gak.global.security.oauth2.CustomFailureHandler;
import com.example.gak.global.security.oauth2.CustomOAuth2AuthorizedClientService;
import com.example.gak.global.security.oauth2.CustomOAuth2UserService;
import com.example.gak.global.security.oauth2.CustomOidcUserService;
import com.example.gak.global.security.oauth2.CustomSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomOAuth2UserService oAuth2UserService;
	private final CustomOidcUserService oidcUserService;
	private final CustomSuccessHandler successHandler;
	private final CustomFailureHandler failureHandler;
	private final JwtAuthFilter jwtAuthFilter;
	private final CustomAuthorizationRequestResolver authorizationRequestResolver;
	private final CustomOAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.oauth2Login(oauth2 -> oauth2
				.authorizationEndpoint(endpoint -> endpoint
					.authorizationRequestResolver(authorizationRequestResolver))
				.userInfoEndpoint(userInfo -> userInfo
					.userService(oAuth2UserService)
					.oidcUserService(oidcUserService))
				.authorizedClientService(oAuth2AuthorizedClientService)
				.successHandler(successHandler)
				.failureHandler(failureHandler))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.GET, "/api/v1/sessions/*").permitAll()
				.requestMatchers(AccessTokenFreeUrls.PATHS).permitAll()
				.anyRequest().authenticated())
			.addFilterAt(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.sessionManagement(s -> s
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(
			"https://localhost:3000",
			"http://localhost:3000",
			"https://api.gak.today",
			"https://gak.today"
		));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
		configurationSource.registerCorsConfiguration("/**", configuration);

		return configurationSource;
	}
}
