package com.example.earthtalk.domain.debate.entity;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FlagType {
    PRO("pro"),
    CON("con"),
    NO_POSITION("noPosition");

    private final String position;

    public static FlagType fromString(String position) {
        if (position == null) {
            return null;
        }

        return Arrays.stream(FlagType.values())
            .filter(flag -> flag.position.equalsIgnoreCase(position))
            .findFirst()
            .orElse(NO_POSITION);
    }
}
