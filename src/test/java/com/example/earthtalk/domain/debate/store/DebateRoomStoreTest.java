package com.example.earthtalk.domain.debate.store;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.entity.SpeakCountType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class DebateRoomStoreTest {

	@Autowired
	private DebateRoomStore debateRoomStore;

	@Test
	public void testPutAndGetAndRemove() {
		UUID testUuid = UUID.randomUUID();
		String uuidString = testUuid.toString();
		// 테스트를 위한 Debate 객체 생성 (예: Debate의 id와 title 필드가 있다고 가정)
		// 예시: Debate 객체를 Builder로 생성하는 코드
		Debate debate = Debate.builder()
			.uuid(testUuid)
			.title("테스트 채팅방")
			.description("이것은 토론 설명 예시입니다.")
			.member(MemberNumberType.T2)
			.continent(ContinentType.AS)
			.category(CategoryType.CO)
			.time(TimeType.T5)
			.status(RoomType.DEBATE)
			.speakCount(SpeakCountType.FIVE)
			.agreeNumber(0L)
			.disagreeNumber(0L)
			.neutralNumber(0L)
			.build();

		// 생성한 Debate 객체를 Redis 기반 DebateRoomStore에 저장
		debateRoomStore.put(debate);


		// 저장된 객체를 Redis에서 조회
		Debate retrieved = debateRoomStore.get(uuidString);
		assertNotNull(retrieved, "저장된 Debate 객체가 조회되어야 합니다.");
		assertEquals("테스트 채팅방", retrieved.getTitle(), "Debate 제목이 일치해야 합니다.");

		// 저장된 객체 삭제
		debateRoomStore.remove(uuidString);
		Debate deleted = debateRoomStore.get(uuidString);
		assertNull(deleted, "삭제 후 조회하면 null이어야 합니다.");
	}
}
