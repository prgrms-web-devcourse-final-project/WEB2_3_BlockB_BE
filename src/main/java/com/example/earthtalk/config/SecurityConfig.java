package com.example.earthtalk.config;

import com.example.earthtalk.domain.oauth.handler.OAuth2LoginFailureHandler;
import com.example.earthtalk.domain.oauth.handler.OAuth2LoginSuccessHandler;
import com.example.earthtalk.domain.oauth.service.CustomOAuth2UserService;
import com.example.earthtalk.global.security.handler.JwtAccessDeniedHandler;
import com.example.earthtalk.global.security.handler.JwtAuthenticationEntryPoint;
import com.example.earthtalk.global.security.handler.JwtAuthenticationFilter;
import com.example.earthtalk.global.security.handler.JwtFilterExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    private static final String[] FRONT_URL = {
        "/earth_talk/**"
    };
    private static final String[] PERMIT_URL_ARRAY = {
        /* Auth */
        "/api-docs/**",
        "/swagger-ui/**",
        "/api/auth/reissue"
    };

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .headers((headerConfig) ->
                headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 관련
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers(FRONT_URL).permitAll()
                .requestMatchers(PERMIT_URL_ARRAY).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            //== 소셜 로그인 설정 ==///
            .oauth2Login((login) -> login
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
                .authorizationEndpoint((auth) -> auth.baseUri("/oauth2/authorization"))
                .userInfoEndpoint((service) -> service.userService(customOAuth2UserService)
            ))
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtFilterExceptionHandler(new ObjectMapper()), JwtAuthenticationFilter.class)
            .exceptionHandling((exceptionHandling) ->
                exceptionHandling
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers(new AntPathRequestMatcher("/favicon.ico"))
            .requestMatchers(new AntPathRequestMatcher("/css/**"))
            .requestMatchers(new AntPathRequestMatcher("/js/**"))
            .requestMatchers(new AntPathRequestMatcher("/image/**"))
            .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**"))
            .requestMatchers(new AntPathRequestMatcher("/test"))
            .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**"))
            .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // frontend url
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}

