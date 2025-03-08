package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.FlagType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteRequest {
	private FlagType vote;
	private Long userId;
}
