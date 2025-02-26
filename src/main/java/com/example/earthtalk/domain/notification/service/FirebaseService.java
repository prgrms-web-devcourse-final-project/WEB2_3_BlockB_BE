package com.example.earthtalk.domain.notification.service;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirebaseService {


    // 푸시 알림 전송 메서드
    public void pushNotification(String token, String content) {
        try {

            // 유효성 검사
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token cannot be null or empty");
            }

            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("Content cannot be null or empty");
            }

            // firebase 기반 notification 객체 생성
            Notification notification = Notification.builder()
                    .setTitle("알림")
                    .setBody(content)
                    .build();

            // notification 객체와 token 값을 이용하여 message 생성
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            // 알림 전송
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            log.info("Error sending message : " + e.getMessage());
            throw new IllegalArgumentException("Error sending message");
        }
    }
}
