package com.example.earthtalk.domain.user.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ObserverChatResponse {
    private String nickname;
    private String content;
    private String profileUrl;
    private LocalDateTime createdAt;
}
