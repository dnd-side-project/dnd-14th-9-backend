package com.example.gak.domain.auth.dto;

public record TokenVerifier(String code, String codeVerifier) {
}
