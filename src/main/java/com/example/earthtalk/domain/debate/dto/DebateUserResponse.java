package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.FlagType;

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
}
