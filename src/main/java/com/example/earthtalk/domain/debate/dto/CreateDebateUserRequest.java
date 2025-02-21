package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.FlagType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDebateUserRequest {

	private Long userId;

	private FlagType position;

}
