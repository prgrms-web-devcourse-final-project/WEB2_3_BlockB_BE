package com.example.earthtalk.controller;

import com.example.earthtalk.domain.notification.dto.request.SaveTokenRequest;
import com.example.earthtalk.domain.notification.dto.request.SendNotificationRequest;
import com.example.earthtalk.domain.notification.dto.response.NotificationListResponse;
import com.example.earthtalk.domain.notification.service.NotificationService;
import com.example.earthtalk.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 접속중인 사용자의 알림을 조회하기 위한 API.
    @Operation(summary = "알림 조회 API 입니다.", description = "userId 의 값을 받아 해당하는 알림들을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<NotificationListResponse>>> getNotificationList(@RequestParam("userId") Long userId) {
        List<NotificationListResponse> responses = notificationService.getNotifications(userId);
        return ResponseEntity.ok(ApiResponse.createSuccess(responses));
    }

    // FE 에서 전송한 토큰값을 저장하기 위한 API.
    @Operation(summary = "FCM 토큰 저장 API 입니다.", description = "토큰 값과 userId 값을 통해 user 테이블에 해당하는 칼럼에 토큰 값을 추가합니다.")
    @PostMapping("/saveToken")
    public ResponseEntity<ApiResponse<?>> saveToken(@RequestBody SaveTokenRequest request) {
        notificationService.saveToken(request);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    /* FE 에서 알림에 관한 기능을 사용하기 위해 만든 API.
     * BE 기준으로 Service 의 sendNotification 메서드만 호출해도 사용할 수 있습니다.
     *
     * 알림 전송 간단 가이드라인
     * 1. SendNotificationRequest 설명 - 필드 값에는 userId, notificationType, typeId 가 들어갑니다.
     *  1) userId - 알림을 받는 대상 id 에 관한 필드값입니다.
     *  2) notificationType - 알림 유형에 관한 필드값입니다.
     *      2-1 REPORT - 신고에 관한 유형입니다.
     *      2-2 FOLLOW - 팔로우에 관한 유형입니다.
     *      2-3 DEBATE - 토론방에 관한 유형입니다.
     *      2-4 CHAT - 채팅방에 관한 유형입니다.
     *      2-x ... - 필요 시 더 추가하여 사용하면 됩니다.
     *  3) typeId - 알림유형에 관한 id 값입니다.
     *      3-1 REPORT - 신고 ID 의 값입니다.
     *      3-2 FOLLOW - 팔로우한 회원 ID 의 값입니다.
     *      3-3 DEBATE - 토론방 ID 값입니다.
     *      3-4 CHAT - 채팅방 ID 값입니다.
     * 2. 알림 전송 로직
     *   - Controller 의 send 는 FE 에서 한 행동에 대해 알림을 보내주기 위해 만든 API 입니다.
     *   - 실질적인 로직은 Service 에 있습니다. (자세한 내용은 Service 에서 설명)
     */
    @Operation(summary = "알림 전송 API 입니다.", description = "userId 에 해당하는 user 에게 알림을 전송하며 알림 유형에 따라 문구가 바뀌고 typeId 를 통해 알림과 관련된 대상을 연결시킬 수 있습니다.")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendNotification(@RequestBody SendNotificationRequest request) throws Exception{
        String response = notificationService.sendNotification(request);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    // 사용자가 알림을 읽었을 때 status 값을 변경하기 위한 API
    @Operation(summary = "전송된 알림의 상태를 변경시키는 API 입니다.", description = "읽지 않은 알림에 대하여 읽음 상태로 변경 시킬 수 있습니다.")
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<?>> readNotification(@PathVariable("notificationId") Long notificationId) {
        notificationService.readNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }
}
