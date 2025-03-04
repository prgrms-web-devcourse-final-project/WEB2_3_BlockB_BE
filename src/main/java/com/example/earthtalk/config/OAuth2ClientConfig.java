package com.example.earthtalk.config;

import com.example.earthtalk.domain.oauth.util.GoogleClient;
import com.example.earthtalk.domain.oauth.util.KakaoClient;
import com.example.earthtalk.domain.oauth.util.NaverClient;
import com.example.earthtalk.domain.oauth.util.OAuthClient;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuth2ClientConfig {
    @Bean
    public Map<String, OAuthClient> oauthClients(
        GoogleClient googleClient,
        NaverClient naverClient,
        KakaoClient kakaoClient
    ) {
        return Map.of(
            "google", googleClient,
            "naver", naverClient,
            "kakao", kakaoClient
        );
    }
}

