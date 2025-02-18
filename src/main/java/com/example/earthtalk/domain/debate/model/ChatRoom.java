package com.example.earthtalk.domain.debate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatRoom {
	private String roomId;
	private int memberNumberType;

}
