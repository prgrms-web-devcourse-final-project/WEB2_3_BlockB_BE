package com.example.earthtalk.domain.notification.service;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    // 푸시 알림 전송 메서드
    public String sendPushNotification(String token, String content) throws Exception {
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
        return FirebaseMessaging.getInstance().send(message);
    }
}
