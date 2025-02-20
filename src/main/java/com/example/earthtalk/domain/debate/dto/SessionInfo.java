package com.example.earthtalk.domain.debate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@ToString
public class SessionInfo {
	private String roomId;
	private String userName;
	private String position;
}
