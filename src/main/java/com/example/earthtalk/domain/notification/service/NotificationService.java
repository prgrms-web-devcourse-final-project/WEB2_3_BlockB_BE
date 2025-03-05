package com.example.earthtalk.domain.notification.service;

import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.notification.dto.request.CheckTokenRequest;
import com.example.earthtalk.domain.notification.dto.request.SaveNotificationRequest;
import com.example.earthtalk.domain.notification.dto.request.SaveTokenRequest;
import com.example.earthtalk.domain.notification.dto.request.SendNotificationRequest;
import com.example.earthtalk.domain.notification.dto.response.CheckTokenResponse;
import com.example.earthtalk.domain.notification.dto.response.NotificationListResponse;
import com.example.earthtalk.domain.notification.entity.Notification;
import com.example.earthtalk.domain.notification.entity.NotificationType;
import com.example.earthtalk.domain.notification.repository.NotificationRepository;
import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.repository.ReportRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.example.earthtalk.global.exception.IllegalArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseService firebaseService;
    private final FcmTokenService fcmTokenService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final DebateRepository debateRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String NOTIFICATION_AGREE_PREFIX = "notification_allowed:";
    private static final String FOLLOW_MESSAGE = "%s님이 당신을 팔로우했습니다.";
    private static final String REPORT_MESSAGE = "%s(으)로 운영자에게 %s을(를) 처분받았습니다.";
    private static final String CHAT_MESSAGE = "참가 중인 채팅방의 대기가 완료되었습니다.";

    // 접속중인 사용자의 id 값을 전달해주면 그와 관련된 알림을 조회하여 반환합니다.
    public List<NotificationListResponse> getNotifications(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        List<Notification> notifications = notificationRepository.getNotifications(user);
        List<NotificationListResponse> responses = new ArrayList<>();
        for (Notification notification : notifications) {
            NotificationListResponse response = NotificationListResponse.from(notification);
            responses.add(response);
        }
        return responses;
    }

    public CheckTokenResponse checkToken(CheckTokenRequest request) {
        userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        boolean isAllow = !isNotificationNotAllowed(request.userId());
        boolean isExist = fcmTokenService.checkFcmToken(request.userId(), request.token());
        return new CheckTokenResponse(isExist, isAllow);
    }

    // FE 에서 받은 토큰을 fcmToken 값을 redis 에 저장하는 메서드
    public void saveToken(SaveTokenRequest request) {
        if(request == null || request.token() == null) {
            throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY);
        }
        userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        String redisKey = NOTIFICATION_AGREE_PREFIX + request.userId();
        if (request.isAllow().equals("true")) {
            redisTemplate.opsForValue().set(redisKey, "true");
        } else {
            redisTemplate.opsForValue().set(redisKey, "false");
        }
        fcmTokenService.saveFcmToken(request.userId(), request.token());
    }

    /*
     * 알림 전송 메서드입니다.
     * 알림 전송 로직에 관한 설명
     * 0. FirebaseConfig.java 클래스를 통해 Firebase 를 초기화시킵니다. (@Configuration)
     * 1. request 의 userId 값을 통해 redis 에서 token 값들을 찾습니다.
     * 2. request 를 통해 content 값을 지정합니다. - getContent(request) 메서드 (필요 시 변경 가능)
     * 3. 만들어진 알림에 대한 정보를 DB 에 저장합니다.
     * 4. token 값들과 content 값으로 firebaseService 의 pushNotification 메서드를 호출합니다.
     * 5. content 값을 통해 firebase 의 Notification 객체를 생성합니다.
     * 6. token 값과 Notification 객체를 통해 Message 객체를 생성합니다.
     * 7. 0번에서 초기화되었던 firebase 를 통해 알림을 전송하고 response 를 반환합니다.
     */
    public void sendNotification(SendNotificationRequest request) {
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (isNotificationNotAllowed(request.userId())) {
            return;
        }

        Set<Object> fcmTokens = fcmTokenService.getFcmTokens(request.userId());
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        String content = request.content() == null ? getContent(request) : request.content();
        SaveNotificationRequest saveNotificationRequest = request.toSave(content);
        notificationRepository.save(saveNotificationRequest.toEntity(user));
        firebaseService.pushNotification(fcmTokens, content);
    }

    // 사용자가 알림을 확인했을 때 status 를 read 로 변경시키는 메서드.
    public void readNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.read();
    }

    public void removeNotification(Long notificationId) {
        notificationRepository.findById(notificationId).orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notificationRepository.deleteById(notificationId);
    }

    // 알림 허용에 대해 거부하는 메서드 - 마이페이지에서 알림 거부할 때 사용.
    public void notAllowNotification(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        String redisKey = NOTIFICATION_AGREE_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, "false");
    }

    // 알림 허용 여부를 redis 에서 가져오는 메서드 - 거부할 시 true
    private boolean isNotificationNotAllowed(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        String redisKey = NOTIFICATION_AGREE_PREFIX + userId;
        String allowedStatus = (String) redisTemplate.opsForValue().get(redisKey);
        if (allowedStatus == null) {
            return true;
        }
        return !allowedStatus.equals("true");
    }

    // notiType 에 따라 content 를 가져오는 메서드.
    private String getContent(SendNotificationRequest request) {
        NotificationType type = request.notificationType();
        if (type == NotificationType.FOLLOW) {
            User user = userRepository.findById(request.typeId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
            return String.format(FOLLOW_MESSAGE, user.getNickname());
        }

        if (type == NotificationType.REPORT) {
            Report report = reportRepository.findById(request.typeId()).orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));
            return String.format(REPORT_MESSAGE, report.getReportType().getValue(), report.getResultType().getValue());
        }

        if (type == NotificationType.CHAT) {
            debateRepository.findById(request.typeId()).orElseThrow(() -> new NotFoundException(ErrorCode.DEBATEROOM_NOT_FOUND));
            return String.format(CHAT_MESSAGE);
        }

        return null;
    }
}
