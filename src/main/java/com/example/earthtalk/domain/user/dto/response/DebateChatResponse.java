package com.example.earthtalk.domain.user.dto.response;

import com.example.earthtalk.domain.debate.entity.FlagType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DebateChatResponse {
    private String nickname;
    private FlagType position;
    private String content;
    private String profileUrl;
    private LocalDateTime createdAt;
}
