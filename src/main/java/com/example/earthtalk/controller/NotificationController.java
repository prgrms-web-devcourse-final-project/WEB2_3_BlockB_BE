package com.example.earthtalk.controller;

import com.example.earthtalk.domain.notification.dto.request.SaveTokenRequest;
import com.example.earthtalk.domain.notification.dto.request.SendNotificationRequest;
import com.example.earthtalk.domain.notification.dto.response.NotificationListResponse;
import com.example.earthtalk.domain.notification.service.NotificationService;
import com.example.earthtalk.global.response.ApiResponse;
import com.google.protobuf.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<NotificationListResponse>>> getNotificationList(@RequestParam("userId") Long userId) {
        List<NotificationListResponse> responses = notificationService.getNotifications(userId);
        return ResponseEntity.ok(ApiResponse.createSuccess(responses));
    }

    @PostMapping("/saveToken")
    public ResponseEntity<ApiResponse<?>> saveToken(@RequestBody SaveTokenRequest request) {
        notificationService.saveToken(request);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendNotification(@RequestBody SendNotificationRequest request) throws Exception{
        String response = notificationService.sendNotification(request);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }
}
