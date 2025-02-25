package com.example.earthtalk.domain.debate.entity;

import lombok.Getter;

@Getter
public enum SpeakCountType {
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7),
	EIGHT(8),
	NINE(9),
	TEN(10);

	private final Integer value;

	SpeakCountType(Integer value) {
		this.value = value;
	}
}
