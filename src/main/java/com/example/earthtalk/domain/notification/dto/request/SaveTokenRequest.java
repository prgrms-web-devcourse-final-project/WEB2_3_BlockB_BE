package com.example.earthtalk.domain.notification.dto.request;

public record SaveTokenRequest(Long userId, String token, String isAllow) {
}
