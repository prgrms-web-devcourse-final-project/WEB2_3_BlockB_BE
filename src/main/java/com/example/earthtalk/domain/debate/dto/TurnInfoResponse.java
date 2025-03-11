package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.FlagType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TurnInfoResponse {
    private Integer time;
    private Integer turnCount;

}
