package com.example.earthtalk.domain.notification.service;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.notification.dto.request.SaveNotificationRequest;
import com.example.earthtalk.domain.notification.dto.request.SaveTokenRequest;
import com.example.earthtalk.domain.notification.dto.request.SendNotificationRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FirebaseService firebaseService;
    private final ReportRepository reportRepository;
    private final DebateRepository debateRepository;

    private static final String FOLLOW_MESSAGE = "%s님이 당신을 팔로우했습니다.";
    private static final String REPORT_MESSAGE = "%s(으)로 운영자에게 %s을(를) 처분받았습니다.";
    private static final String DEBATE_MESSAGE = "참가 중인 토론방의 대기가 완료되었습니다.";

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

    // FE 에서 받은 토큰을 fcmToken 값을 user 테이블에 저장하는 메서드입니다.
    public void saveToken(SaveTokenRequest request) {
        User user = request.toEntity(userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND)));
        userRepository.save(user);
    }

    /*
     * 알림 전송 메서드입니다.
     * 알림 전송 로직에 관한 설명
     * 0. FirebaseConfig.java 클래스를 통해 Firebase 를 초기화시킵니다. (@Configuration)
     * 1. request 의 userId 값을 통해 user 객체를 찾습니다.
     * 2. user 객체를 통해 token 값을 찾습니다.
     * 3. request 를 통해 content 값을 지정합니다. - getContent(request) 메서드 (필요 시 변경 가능)
     * 4. 만들어진 알림에 대한 정보를 DB 에 저장합니다.
     * 5. token 값과 content 값으로 firebaseService 의 pushNotification 메서드를 호출합니다.
     * 6. content 값을 통해 firebase 의 Notification 객체를 생성합니다.
     * 7. token 값과 Notification 객체를 통해 Message 객체를 생성합니다.
     * 8. 0번에서 초기화되었던 firebase 를 통해 알림을 전송하고 response 를 반환합니다.
     */
    public void sendNotification(SendNotificationRequest request) throws Exception {
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        String fcmToken = user.getFCMToken();
        if (fcmToken == null) {
            return;
        }

        String content = request.content();
        if (content == null) {
            content = getContent(request);
        }

        SaveNotificationRequest saveNotificationRequest = request.toSave(content);
        notificationRepository.save(saveNotificationRequest.toEntity(user));
        firebaseService.pushNotification(fcmToken, content);
    }

    // 사용자가 알림을 확인했을 때 status 를 read 로 변경시키는 메서드.
    public void readNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.read();
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

        if (type == NotificationType.DEBATE || type == NotificationType.CHAT) {
            debateRepository.findById(request.typeId()).orElseThrow(() -> new NotFoundException(ErrorCode.DEBATEROOM_NOT_FOUND));
            return String.format(DEBATE_MESSAGE);
        }

        return null;
    }
}
