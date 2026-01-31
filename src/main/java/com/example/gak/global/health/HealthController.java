package com.example.gak.global.health;

import com.example.gak.global.apiPayload.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map> health() {
        return ApiResponse.onSuccess(Map.of("status", "UP"));
    }
}