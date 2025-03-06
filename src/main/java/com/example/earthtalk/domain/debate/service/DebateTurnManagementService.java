package com.example.earthtalk.domain.debate.service;

import com.example.earthtalk.domain.debate.entity.FlagType;
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
    private final SimpMessagingTemplate messagingTemplate;

    public void createDebateTurn(UUID roomId, SpeakCountType speakCountType) {
        debateTurns.put(roomId, FlagType.PRO);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() ->
            switchTurn(roomId), speakCountType.getValue(), speakCountType.getValue(), TimeUnit.MINUTES);
        turnScheduler.put(roomId, scheduler);
    }

    private void switchTurn(UUID roomId) {
        FlagType currentTurn = debateTurns.get(roomId);
        FlagType nextTurn = switch (currentTurn) {
            case PRO -> FlagType.CON;
            case CON -> FlagType.PRO;
            case NO_POSITION -> FlagType.NO_POSITION;
        };

        Map<String, Object> message = Map.of(
            "event", "turn_change",
            "turn", nextTurn
        );
        messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(),message);
    }

    public void removeDebateTurn(UUID roomId) {
        debateTurns.remove(roomId);
        turnScheduler.remove(roomId);
    }

}
