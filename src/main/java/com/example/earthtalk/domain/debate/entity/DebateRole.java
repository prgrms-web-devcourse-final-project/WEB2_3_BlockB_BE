package com.example.earthtalk.domain.debate.entity;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DebateRole {
	PARTICIPANT("participant"),
	OBSERVER("observer");

	private final String role;

	public static DebateRole fromString(String role) {
		if (role == null) {
			return null;
		}

		return Arrays.stream(DebateRole.values())
			.filter(flag -> flag.role.equalsIgnoreCase(role))
			.findFirst()
			.orElse(OBSERVER);
	}
}
