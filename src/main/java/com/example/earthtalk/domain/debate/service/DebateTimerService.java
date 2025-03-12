package com.example.earthtalk.domain.debate.service;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.EventType;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.entity.SpeakCountType;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.store.VoteStore;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.IllegalArgumentException;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class DebateTimerService {

    private final DebateTurnManagementService debateTurnManagementService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DebateRepository debateRepository;
    private final VoteStore voteStore;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(50);
    private final Map<UUID, ScheduledFuture<?>> debateTimers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> voteTimers = new ConcurrentHashMap<>();

    public void startDebateTimer(UUID roomId, TimeType timeType, SpeakCountType speakCountType) {
        debateTimers.put(roomId,
            scheduler.schedule(() -> endDebate(roomId),
                (long) timeType.getValue() * speakCountType.getValue() * 2 - 30, TimeUnit.SECONDS));

        Map<String, Object> message = Map.of(
            "event", EventType.NOTIFICATION,
            "message", "잠시 후 토론이 시작됩니다... "
        );
        messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message);
        scheduler.schedule(
            ()-> debateTurnManagementService.createDebateTurn(roomId,timeType, speakCountType)
            ,5, TimeUnit.SECONDS);

        debateTurnManagementService.createDebateTurn(roomId,timeType, speakCountType);
        System.out.println("Debate started for " + roomId);
    }

    private void startVoteTimer(UUID roomId) {
        Map<String, Object> message1 = Map.of(
            "event", EventType.NOTIFICATION,
            "message", "잠시 후 투표가 시작됩니다."
        );
        messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message1);

        scheduler.schedule(()-> {

            voteTimers.put(roomId,
                scheduler.schedule(() -> endVote(roomId), 20, TimeUnit.SECONDS));

            Debate debate = debateRepository.findByUuid(roomId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND));
            debate.updateRoomType(RoomType.VOTING);
            debateRepository.save(debate);

            voteStore.startVote(roomId);

            Map<String, Object> message = Map.of(
                "event", EventType.STATUS,
                "status", RoomType.VOTING,
                "message", "투표가 시작되었습니다."
            );
            messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message);
            System.out.println("vote started for " + roomId);
        }, 3, TimeUnit.SECONDS);
    }

    private void endDebate(UUID roomId) {
        Map<String, Object> message1 = Map.of(
            "event", EventType.NOTIFICATION,
            "message", "30초 후 토론이 종료됩니다."
        );
        messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message1);
        System.out.println("Debate ended in 30 seconds for " + roomId);
        scheduler.schedule(() -> {

            debateTurnManagementService.removeDebateTurn(roomId);
            Map<String, Object> message2 = Map.of(
                "event", EventType.NOTIFICATION,
                "message", "토론이 종료되었습니다."
            );
            messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message2);
            Debate debate = debateRepository.findByUuid(roomId)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND));
            if(debate.isResultEnabled()) {
                startVoteTimer(roomId);
            } else {
                closeDebate(roomId);
            }
            System.out.println("Debate ended for " + roomId);
        }, 30, TimeUnit.SECONDS);
    }

    private void endVote(UUID roomId) {
        Map<String, Object> message = Map.of(
            "event", EventType.NOTIFICATION,
            "message", "투표 종료 10초 전입니다."
        );
        messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message);
        System.out.println("10 seconds left to end vote for " + roomId);

        scheduler.schedule(() -> {
                Map<String, Object> message2 = Map.of(
                    "event", EventType.NOTIFICATION,
                    "message", "투표가 종료되었습니다. 투표 결과 집계중..."
                );
                messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message2);
                closeDebate(roomId);
                System.out.println("vote ended for " + roomId);
            }, 10, TimeUnit.SECONDS);
    }

    private void closeDebate(UUID roomId) {
        Debate debate = debateRepository.findByUuid(roomId)
            .orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND));
        debate.updateRoomType(RoomType.CLOSED);
        debateRepository.save(debate);

        log.info("Vote resultEnabled for {} : {}", roomId, debate.isResultEnabled());
        if(debate.isResultEnabled()) {
            FlagType flag = voteStore.getVoteResult(roomId);
            log.info("Vote result flag for " + roomId+ " : " + flag);
            String reuslt = "";
            switch(flag) {
                case PRO -> reuslt = "찬성 팀이 승리했습니다!!";
                case CON -> reuslt = "반대 팀이 승리했습니다!!";
                case NO_POSITION -> reuslt = "비겼습니다!!";
            }
            Map<String, Object> message = Map.of(
                "event", EventType.STATUS,
                "status", RoomType.CLOSED,
                "message", "토론이 모두 종료되었습니다." + reuslt
            );
            messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message);
        }else{
            Map<String, Object> message = Map.of(
                "event", EventType.STATUS,
                "status", RoomType.CLOSED,
                "message", "토론이 모두 종료되었습니다."
            );
            messagingTemplate.convertAndSend("/topic/debate/" + roomId.toString(), message);
        }
        System.out.println("Debate Finished for " + roomId);
        debateTimers.remove(roomId);
        voteTimers.remove(roomId);
    }
}
