package com.example.earthtalk.domain.user.userDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoDto {
    private Long userId;
    private String nickname;
    private String profile;
    private String introduction;
    private Long totalFollowers;
    private Long totalFollowees;
    private Long wins;
    private Long draws;
    private Long losses;
}
