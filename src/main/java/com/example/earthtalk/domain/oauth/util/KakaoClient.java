package com.example.earthtalk.domain.oauth.util;

import com.example.earthtalk.domain.oauth.dto.request.SocialAccessTokenRequest;
import com.example.earthtalk.domain.oauth.dto.response.accessToken.KakaoAccessTokenResponse;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.OAuth2AuthenticationException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KakaoClient implements OAuthClient {

    private final RestTemplate restTemplate;
    private static final String TOKEN_PREFIX = "Bearer ";

    @Autowired
    public KakaoClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public String getAccessTokenFromAuthCode(ClientRegistration clientRegistration, String authCode) {
        String decodedAuthCode = decodeAuthCode(authCode);
        HttpEntity<MultiValueMap<String, String>> httpEntity = createHttpEntityForRequestAccessToken(
            clientRegistration, decodedAuthCode);

        KakaoAccessTokenResponse response = requestKakaoOauthAPIServer(clientRegistration, httpEntity);
        System.out.println(response);

        return Optional.ofNullable(response)
            .orElseThrow(() -> new OAuth2AuthenticationException(ErrorCode.INVALID_OAUTH_TOKEN))
            .getAccessToken();
    }

    @Override
    public Map<String, Object> getProfileFromAccessToken(ClientRegistration clientRegistration, String accessToken) {
        HttpEntity<Void> httpEntity = createHttpEntityForRequestResource(clientRegistration, accessToken);

        Map<String, Object> response = requestKakaoResourceServer(clientRegistration, httpEntity);
        System.out.println(response);

        return Optional.ofNullable(response)
            .orElseThrow(() -> new OAuth2AuthenticationException(ErrorCode.OAUTH_SERVER_ERROR));
    }

    /**
     * authCode에 포함된 역슬래시 문자 인식 문제로, 디코딩 과정을 거쳐야 함.
     */
    private String decodeAuthCode(String authCode) {
        return URLDecoder.decode(authCode, StandardCharsets.UTF_8);
    }

    private HttpEntity<MultiValueMap<String, String>> createHttpEntityForRequestAccessToken(
        ClientRegistration clientRegistration, String authCode) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return new HttpEntity<>(
            new SocialAccessTokenRequest(
                authCode,
                clientRegistration.getClientId(),
                clientRegistration.getClientSecret(),
                clientRegistration.getRedirectUri())
                .toFormData(),
            headers
        );
    }

    private HttpEntity<Void> createHttpEntityForRequestResource(ClientRegistration clientRegistration, String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        return new HttpEntity<>(headers);
    }

    private KakaoAccessTokenResponse requestKakaoOauthAPIServer(
        ClientRegistration clientRegistration, HttpEntity<MultiValueMap<String, String>> httpEntity) {

        return restTemplate.exchange(
            clientRegistration.getProviderDetails().getTokenUri(),
            HttpMethod.POST,
            httpEntity,
            KakaoAccessTokenResponse.class
        ).getBody();
    }

    private Map requestKakaoResourceServer(ClientRegistration clientRegistration, HttpEntity<Void> httpEntity) {
        return restTemplate.exchange(
            clientRegistration.getProviderDetails()
                .getUserInfoEndpoint().getUri(),
            HttpMethod.GET,
            httpEntity,
            Map.class
        ).getBody();
    }
}

