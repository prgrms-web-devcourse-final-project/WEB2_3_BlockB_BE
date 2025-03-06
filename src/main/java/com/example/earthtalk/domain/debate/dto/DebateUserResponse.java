package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DebateUserResponse {
	private Long id;

	private String email;

	private String nickname;

	private String introduction;

	private String profileUrl;

	private Long winNumber = 0L;

	private Long drawNumber = 0L;

	private Long defeatNumber = 0L;

	public static DebateUserResponse fromEntity(User user) {
		return DebateUserResponse.builder()
			.id(user.getId())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.introduction(user.getIntroduction())
			.profileUrl(user.getProfileUrl())
			.winNumber(user.getWinNumber())
			.drawNumber(user.getDrawNumber())
			.defeatNumber(user.getDefeatNumber())
			.build();
	}
}
