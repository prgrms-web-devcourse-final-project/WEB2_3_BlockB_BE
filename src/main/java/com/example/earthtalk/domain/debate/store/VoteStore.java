package com.example.earthtalk.domain.debate.store;

import com.example.earthtalk.domain.debate.dto.VoteRequest;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.VoteStatus;
import com.example.earthtalk.domain.debate.service.DebateRoomService;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.IllegalArgumentException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class VoteStore {
    private final DebateRoomService debateRoomService;
    private final Map<UUID, VoteStatus> votes = new ConcurrentHashMap<>();

    public void startVote(UUID roomId){
        votes.put(roomId,new VoteStatus());
    }

    public void processVote(UUID roomId, VoteRequest voteRequest) {
        if (!votes.containsKey(roomId)) {
            throw new IllegalArgumentException(ErrorCode.VOTE_NOT_STARTED);
        }
        if (!votes.get(roomId).getUsers().contains(voteRequest.getUserId())) {
            throw new IllegalArgumentException(ErrorCode.VOTE_DUPLICATED);
        }

        FlagType flag = voteRequest.getVote();
        VoteStatus voteStatus = votes.get(roomId);
        switch (flag) {
            case PRO:
                voteStatus.incrementPro();
                voteStatus.getUsers().add(voteRequest.getUserId());
                break;
            case CON:
                voteStatus.incrementCon();
                voteStatus.getUsers().add(voteRequest.getUserId());
                break;
            case NO_POSITION:
                voteStatus.incrementNeutral();
                voteStatus.getUsers().add(voteRequest.getUserId());
                break;
            default:
                break;
        }
        votes.put(roomId, voteStatus);
    }

    public FlagType getVoteResult(UUID roomId) {
        if (!votes.containsKey(roomId)) {
            throw new IllegalArgumentException(ErrorCode.VOTE_NOT_STARTED);
        }
        return debateRoomService.processDebateResult(roomId, votes.get(roomId));
    }

}
