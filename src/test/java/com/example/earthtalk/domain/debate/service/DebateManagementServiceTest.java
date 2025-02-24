package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.constant.ContinentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class DebateManagementServiceTest {

	private DebateParticipantsRepository debateParticipantsRepository;
	private DebateChatRepository debateChatRepository;
	private DebateRepository debateRepository;
	private UserRepository userRepository;
	private DebateRoomService debateRoomService;
	private DebateManagementService debateManagementService;

	@BeforeEach
	public void setUp() {
		debateParticipantsRepository = Mockito.mock(DebateParticipantsRepository.class);
		debateChatRepository = Mockito.mock(DebateChatRepository.class);
		debateRepository = Mockito.mock(DebateRepository.class);
		userRepository = Mockito.mock(UserRepository.class);
		debateRoomService = Mockito.mock(DebateRoomService.class);

		debateManagementService = new DebateManagementService(
			debateParticipantsRepository,
			debateChatRepository,
			debateRepository,
			userRepository,
			debateRoomService
		);
	}

	@Test
	public void persistChatRoomIfFull_whenRoomIsFull_shouldPersistDebateAndUsers() {
		// given
		String roomId = "room1";

		// Debate 엔티티 생성: UUID, 제목, 설명, 멤버 수(예: MemberNumberType.T2가 getValue()로 1을 반환한다고 가정),
		// 대륙, 카테고리, 시간, 상태, 좋아요/싫어요 초기값 설정.
		Debate debate = Debate.builder()
			.uuid(UUID.fromString("00000000-0000-0000-0000-000000000001"))
			.title("Test Room")
			.description("Test Subtitle")
			.member(MemberNumberType.T2)   // 여기서 T2가 최대 참여 인원 1이라고 가정
			.continent(ContinentType.AS)
			.category(CategoryType.CO)
			.time(TimeType.T5)
			.status(RoomType.DEBATE)
			.agreeNumber(0L)
			.disagreeNumber(0L)
			.build();

		// debateRepository가 Debate 엔티티를 반환하도록 stub 처리
		when(debateRepository.findByUuid(UUID.fromString(roomId)))
			.thenReturn(Optional.of(debate));

		// "방이 꽉 찼다"를 테스트하기 위해 proUserNames와 conUserNames의 크기를 Debate.member의 값과 동일하게 함
		Set<String> proUserNames = Set.of("proUser");
		Set<String> conUserNames = Set.of("conUser");

		// UserRepository에서 각 사용자 닉네임으로 User를 조회하도록 stub 처리
		User proUser = Mockito.mock(User.class);
		User conUser = Mockito.mock(User.class);
		when(userRepository.findByNickname("proUser")).thenReturn(Optional.of(proUser));
		when(userRepository.findByNickname("conUser")).thenReturn(Optional.of(conUser));

		// when: persistChatRoomIfFull 호출 (Debate 엔티티와 참여자 이름 셋을 인자로 전달)
		debateManagementService.persistChatRoomIfFull(debate, proUserNames, conUserNames);

		// then: Debate 엔티티 저장 검증
		ArgumentCaptor<Debate> debateCaptor = ArgumentCaptor.forClass(Debate.class);
		verify(debateRepository, times(1)).save(debateCaptor.capture());
		Debate savedDebate = debateCaptor.getValue();
		assertEquals("Test Room", savedDebate.getTitle());
		assertEquals("Test Subtitle", savedDebate.getDescription());
		assertEquals(MemberNumberType.T2, savedDebate.getMember());
		// 예시: ContinentType.AS가 "아시아"로 매핑, CategoryType.CO가 "칼럼", TimeType.T5가 5로 매핑된다고 가정
		assertEquals("아시아", savedDebate.getContinent().getValue());
		assertEquals("칼럼", savedDebate.getCategory().getValue());
		assertEquals(5, savedDebate.getTime().getValue());
		assertEquals(RoomType.DEBATE, savedDebate.getStatus());
		assertEquals(0L, savedDebate.getAgreeNumber());
		assertEquals(0L, savedDebate.getDisagreeNumber());

		// DebateParticipants 저장 검증 (프로와 콘 사용자 각각에 대해)
		ArgumentCaptor<DebateParticipants> participantCaptor = ArgumentCaptor.forClass(DebateParticipants.class);
		verify(debateParticipantsRepository, times(2)).save(participantCaptor.capture());
		List<DebateParticipants> participants = participantCaptor.getAllValues();
		boolean foundPro = participants.stream().anyMatch(dp -> dp.getPosition() == FlagType.PRO);
		boolean foundCon = participants.stream().anyMatch(dp -> dp.getPosition() == FlagType.CON);
		assertTrue(foundPro, "Pro user should be persisted");
		assertTrue(foundCon, "Con user should be persisted");

		// DebateRoom 관련 임시 데이터 제거 검증
		verify(debateRoomService, times(1)).removeDebateRoom(debate.getUuid().toString());
	}


}
