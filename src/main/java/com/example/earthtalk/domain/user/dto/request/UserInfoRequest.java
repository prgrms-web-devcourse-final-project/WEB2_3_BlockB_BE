package com.example.earthtalk.domain.user.dto.request;

public record UserInfoRequest() {
    public record ConfirmNickname(String nickname) {
        public static UserInfoRequest.ConfirmNickname of(String nickname) {
            return new UserInfoRequest.ConfirmNickname(nickname);
        }
    }

    public record Signup(
        String nickname,
        String introduction
    ) {
        public static UserInfoRequest.Signup of(String nickname, String introduction) {
            return new UserInfoRequest.Signup(nickname, introduction);
        }
    }
}
