package com.example.earthtalk.domain.debate.service;

import com.example.earthtalk.domain.debate.entity.EventType;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.entity.SpeakCountType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DebateTurnManagementService {

    private final Map<UUID, ScheduledExecutorService> turnScheduler = new ConcurrentHashMap<>();
    private final Map<UUID, FlagType> debateTurns = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(100);
    private final SimpMessagingTemplate messagingTemplate;

    public void createDebateTurn(UUID roomId, SpeakCountType speakCountType) {
        debateTurns.put(roomId, FlagType.NO_POSITION);
        scheduler.scheduleAtFixedRate(() ->
            switchTurn(roomId), 20, speakCountType.getValue() * 60 - 10, TimeUnit.SECONDS);
        turnScheduler.put(roomId, scheduler);
        System.out.println("Create debate turn for " + roomId);
    }

    private void switchTurn(UUID roomId) {
        if (debateTurns.get(roomId) == FlagType.NO_POSITION) {
            Map<String, Object> message = Map.of(
                "event", EventType.STATUS,
                "status", RoomType.DEBATE,
                "message", "토론이 시작되었습니다."
            );
            debateTurns.put(roomId, FlagType.PRO);
            messagingTemplate.convertAndSend("/topic/debate" + roomId, message);
            System.out.println("Debate Started for " + roomId);
            return;
        }

        Map<String, Object> message1 = Map.of(
            "event", EventType.NOTIFICATION,
            "message", "10초 후 턴이 바뀝니다..."
        );
        messagingTemplate.convertAndSend("/topic/debate" + roomId, message1);
        System.out.println("10 Seconds to change turn.... for " + roomId);

        FlagType currentTurn = debateTurns.get(roomId);
        FlagType nextTurn = switch (currentTurn) {
            case PRO -> FlagType.CON;
            case CON -> FlagType.PRO;
            case NO_POSITION -> FlagType.NO_POSITION;
        };
        String turn = nextTurn == FlagType.PRO ? "찬성" : "반대";
        scheduler.schedule(() -> {
            Map<String, Object> message2 = Map.of(
                "event", EventType.TURN,
                "turn", nextTurn,
                "message", turn + "팀 발언이 시작되었습니다."
            );
            messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message2);
            debateTurns.put(roomId, nextTurn);
            System.out.println("Turn changed for " + roomId);
        }, 10, TimeUnit.SECONDS);  // 10초 후에 실행
    }

    public void removeDebateTurn(UUID roomId) {
        debateTurns.remove(roomId);
        System.out.println("Remove debate turn for " + roomId);

    }

}
