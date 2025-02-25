package com.example.earthtalk.domain.debate.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteResponse {
	private Long agreeNumber;
	private Long disagreeNumber;
	private Long neutralNumber;
}
