package com.example.earthtalk.domain.notification.service;

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

    public void saveToken(SaveTokenRequest request) {
        User user = request.toEntity(userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND)));
        userRepository.save(user);
    }

    public String sendNotification(SendNotificationRequest request) throws Exception {
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        String token = user.getFCMToken();
        String content = getContent(request);
        return firebaseService.sendPushNotification(token, content);
    }

    public String getContent(SendNotificationRequest request) {
        NotificationType type = request.notificationType();
        StringBuilder sb = new StringBuilder();
        if (type == NotificationType.FOLLOW) {
            User user = userRepository.findById(request.typeId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
            sb.append(user.getNickname());
            sb.append("님이 당신을 팔로우했습니다.");
        }

        if (type == NotificationType.REPORT) {
            Report report = reportRepository.findById(request.typeId()).orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));
            sb.append(report.getReportType().getValue());
            sb.append("(으)로 운영자에게 ");
            sb.append(report.getResultType().getValue());
            sb.append("을(를) 처분받았습니다.");
        }

        if (type == NotificationType.DEBATE) {
            sb.append("참가 중인 토론방의 대기가 완료되었습니다.");
        }

        if (type == NotificationType.CHAT) {
            sb.append("참관 중인 토론방이 곧 시작합니다.");
        }

        return sb.toString();
    }
}
