package com.example.earthtalk.domain.debate.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.service.ChatRoomService;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.debate.controller.ChatRoomController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ChatRoomControllerTest {

	private MockMvc mockMvc;
	private ChatRoomService chatRoomService; // 목 객체
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
		// ChatRoomService 목 객체 생성
		chatRoomService = Mockito.mock(ChatRoomService.class);
		// ChatRoomController 인스턴스 생성 후 목 객체 주입
		ChatRoomController controller = new ChatRoomController(chatRoomService);
		// standaloneSetup을 통해 MockMvc 인스턴스 생성
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("채팅방 생성 요청 시 올바른 roomId 반환")
	public void createRoom_returnsRoomId() throws Exception {
		// given: 테스트용 CreateDebateRoomRequest 생성 및 설정
		CreateDebateRoomRequest request = new CreateDebateRoomRequest();
		request.setTitle("테스트 토론방");
		request.setDescription("토론방 부제 또는 설명");
		request.setMemberNumber(MemberNumberType.T2);
		// 필요한 추가 메타데이터가 있다면 설정

		// 예상되는 roomId (예: in-memory용 UUID)
		String expectedRoomId = "test-uuid-1234";
		when(chatRoomService.createChatRoom(Mockito.any(CreateDebateRoomRequest.class)))
			.thenReturn(expectedRoomId);


		// when & then: POST 요청 수행 및 결과 검증
		mockMvc.perform(post("/api/chat/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(content().string(expectedRoomId));
	}
}
