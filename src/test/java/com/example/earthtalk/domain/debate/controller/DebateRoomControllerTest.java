package com.example.earthtalk.domain.debate.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.service.DebateRoomService;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class DebateRoomControllerTest {

	private MockMvc mockMvc;
	private DebateRoomService debateRoomService; // 목 객체
	private ObjectMapper objectMapper;

	@BeforeAll
	public static void initEnvironment() {
		// 테스트 실행 전에 시스템 속성으로 환경변수를 설정
		System.setProperty("DB_URL", "jdbc:mariadb://localhost:3306/earthtalk");
		System.setProperty("DB_USERNAME", "root");
		System.setProperty("DB_PASSWORD", "!123456");
	}

	@BeforeEach
	public void setup() {
		// DebateRoomService 목 객체 생성
		debateRoomService = Mockito.mock(DebateRoomService.class);
		// ChatRoomController 인스턴스 생성 후 목 객체 주입
		ChatRoomController controller = new ChatRoomController(debateRoomService);
		// standaloneSetup을 통해 MockMvc 인스턴스 생성
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		objectMapper = new ObjectMapper();
	}

}
