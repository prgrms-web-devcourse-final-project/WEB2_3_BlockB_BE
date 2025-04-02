package com.example.earthtalk.domain.debate.component;

import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.example.earthtalk.global.security.handler.JwtAuthenticationFilter;
import com.example.earthtalk.global.security.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserIdInterceptor implements HandshakeInterceptor {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        log.info("알림 webSocket 연결 시도");

        try {
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
                HttpServletRequest httpServletRequest = servletServerHttpRequest.getServletRequest();

                String token = jwtTokenProvider.parseBearerToken(httpServletRequest.getHeader(JwtAuthenticationFilter.AUTHORIZATION_HEADER));
                if(token != null && jwtTokenProvider.validateAccessToken(token)) {
                    Claims claims = jwtTokenProvider.getClaims(token);
                    User user = userRepository.findByEmail(claims.getSubject()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
                    attributes.put("userId", user.getId());
                    log.info("알림 webSocket 연결 성공 : userId {}", user.getId());
                    return true;
                }
            }

        } catch (Exception e) {
            log.info("알림 webSocket Jwt 검증 실패 : {}", e.getMessage());
        }

        log.info("알림 webSocket 연결 실패");

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {

    }
}
