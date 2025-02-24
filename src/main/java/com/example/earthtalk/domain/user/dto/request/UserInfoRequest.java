package com.example.earthtalk.domain.user.dto.request;

public record UserInfoRequest(
    String nickname
) {
    public static UserInfoRequest from(String nickname) {
        return new UserInfoRequest(nickname);
    }
}
