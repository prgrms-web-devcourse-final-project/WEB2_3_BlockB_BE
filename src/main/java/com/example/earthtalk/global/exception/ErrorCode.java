package com.example.earthtalk.global.exception;

import lombok.Getter;

/**
 * 오류 HttpStatus 코드 메세지 정의 enum 클래스
 */
@Getter
public enum ErrorCode { // 예외 발생시, body에 실어 날려줄 상태, code, message 커스텀

    //-1000: USER
    USER_ALREADY_EXIST(400, -1001, "해당 이메일이 이미 존재합니다."),
    WRONG_SIGNUP(400, -1002, "올바르지 않은 회원가입입니다."),
    INVALID_PASSWORD(400, -1003, "비빌번호가 올바르지 않습니다."),
    USER_NOT_FOUND(404, -1004, "존재하지 않는 사용자입니다."),
    NONEXISTENT_USER(400, -1009, "존재하지 않는 회원입니다."),

    //-2000: JWT
    EMPTY_JWT_TOKEN(400, -2000, "JWT 토큰이 없습니다."),
    INVALID_ACCESS_TOKEN(400, -2001, "유효하지 않은 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(400, -2002, "어세스 토큰이 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(400, -2003, "리프레시 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(400, -2004, "잘못된 리프레시 토큰입니다."),
    INVALID_AUTHORITY_TOKEN(400, -2005, "권한 정보가 없는 토큰입니다."),

    //-3000: COMMON
    NOT_FOUND(404, -3000, "잘못된 경로입니다."),
    BAD_REQUEST(400, -3001, "유효하지 않은 요청입니다."),
    INVALID_REQUEST_BODY(400, -3002, "전달된 데이터가 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(405, -3003,"잘못된 Http Method 입니다."),
    INTERNAL_SERVER_ERROR(500, -3004, "서버 내부 오류입니다."),
    UNAUTHORIZED(401, -3005, "토큰 정보가 만료되었거나 존재하지 않습니다."),
    FORBIDDEN(403, -3006, "접근 권한이 없습니다."),
    INVALID_SORT_TYPE(400, -3007, "올바르지 않은 정렬 타입입니다."),

    //-4000 비즈니스 로직1
    REPORT_NOT_FOUND(404, -4000, "조회된 신고가 존재하지 않습니다."),
    CHAT_NOT_FOUND(404, -4001, "조회된 채팅이 존재하지 않습니다."),
    //-5000 비즈니스 로직2
    TOO_MANY_PARTICIPANTS(409, -5001, "참가자가 이미 최대 인원 입니다.");

    // 1. status = 날려줄 상태코드
    // 2. code = 해당 오류가 어느부분과 관련있는지 카테고리화 해주는 코드. 예외 원인 식별하기 편하기에 추가
    // 3. message = 발생한 예외에 대한 설명.
    private final int status;
    private final int code;
    private final String message;

    ErrorCode(int status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
