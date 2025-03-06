package com.example.earthtalk.domain.notification.service;


import com.example.earthtalk.global.exception.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
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
    private final FcmTokenService fcmTokenService;

    public void pushNotification(Set<Object> tokens, String content, Long userId, String notificationString) {
        try {
            if (tokens == null || tokens.isEmpty() || content == null || content.isEmpty()) {
                throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY);
            }

            Notification notification = Notification.builder()
                    .setTitle("알림")
                    .setBody(content)
                    .setImage(notificationString)
                    .build();

            for (Object tokenObj : tokens) {
                if (!(tokenObj instanceof String)) {
                    log.info("Invalid token type: " + tokenObj);
                    continue;
                }
                String token = (String) tokenObj;

                if (token.isEmpty()) {
                    log.info("Skipping empty token");
                    continue;
                }

                Message message = Message.builder()
                        .setToken(token)
                        .setNotification(notification)
                        .build();

                try {
                    String response = firebaseMessaging.send(message);
                    log.info("FCM Response: " + response);
                } catch (FirebaseMessagingException e) {
                    log.error("FCM send failed: " + e.getMessage());
                    if (e.getMessage().equals("Requested entity was not found.")) {
                        fcmTokenService.removeFcmToken(userId, token);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error sending push notification: " + e.getMessage(), e);
            throw new IllegalArgumentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
