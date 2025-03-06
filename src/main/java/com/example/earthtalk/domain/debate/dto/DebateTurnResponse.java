package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.FlagType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DebateTurnResponse {
    private String event;
    private FlagType turn;
}
