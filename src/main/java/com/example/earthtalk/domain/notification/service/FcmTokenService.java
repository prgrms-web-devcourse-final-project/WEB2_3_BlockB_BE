package com.example.earthtalk.domain.notification.service;

import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private static final String FCM_TOKEN_PREFIX = "fcm_tokens:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    // 토큰값을 저장하는 메서드
    public void saveFcmToken(Long userId, String token) {
        String redisKey = FCM_TOKEN_PREFIX + userId;

        // 기존 토큰을 삭제 후 다시 토큰을 저장 - 토큰 유효기간 관리
        redisTemplate.opsForSet().remove(redisKey, token);
        redisTemplate.opsForSet().add(redisKey, token);
    }

    // 저장된 토큰값을 조회하는 메서드
    public Set<Object> getFcmTokens(Long userId) {
        String redisKey = FCM_TOKEN_PREFIX + userId;
        return redisTemplate.opsForSet().members(redisKey);
    }

    // redis 에 저장된 토큰값을 삭제하는 명령어 - 로그아웃과 웹 닫기등에서 사용
    public void removeFcmToken(Long userId, String token) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        String redisKey = FCM_TOKEN_PREFIX + userId;
        redisTemplate.opsForSet().remove(redisKey, token);
    }
}
