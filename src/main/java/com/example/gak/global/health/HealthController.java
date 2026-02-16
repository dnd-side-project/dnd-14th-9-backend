package com.example.gak.global.health;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gak.global.apiPayload.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "헬스 체크 API")
@RestController
public class HealthController {

	@Operation(summary = "헬스 체크 API")
	@GetMapping("/health")
	public ApiResponse<Map<String, String>> health() {
		return ApiResponse.onSuccess(Map.of("status", "UP"));
	}
}