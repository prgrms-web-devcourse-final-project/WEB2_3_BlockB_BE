package com.example.earthtalk.domain.notification.service;


import com.example.earthtalk.global.exception.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.example.earthtalk.global.exception.IllegalArgumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

    private final FirebaseMessaging firebaseMessaging;

    // 푸시 알림 전송 메서드
    public void pushNotification(Set<Object> tokens, String content) {
        try {

            // 유효성 검사
            if (tokens == null || tokens.isEmpty()) {
                throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY);
            }

            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY);
            }

            // firebase 기반 notification 객체 생성
            Notification notification = Notification.builder()
                    .setTitle("알림")
                    .setBody(content)
                    .build();

            for (Object token : tokens) {
                // notification 객체와 token 값을 이용하여 message 생성
                Message message = Message.builder()
                        .setToken((String) token)
                        .setNotification(notification)
                        .build();

                // 알림 전송
                firebaseMessaging.send(message);
            }
        } catch (Exception e) {
            log.info("Error sending message : " + e.getMessage());
            throw new IllegalArgumentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
