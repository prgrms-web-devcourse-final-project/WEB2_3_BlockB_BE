package com.example.earthtalk.domain.debate.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.FlagType;

public interface DebateParticipantsRepository extends JpaRepository<DebateParticipants, Long> {
	Optional<DebateParticipants> findByDebate_UuidAndUser_Nickname(UUID debateUuid, String username);
	List<DebateParticipants> findByDebate_Uuid(UUID debateUuid);
	List<DebateParticipants> findByDebate_UuidAndPosition(UUID debateUuid, FlagType position);

    void deleteAllByUserId(Long id);
}
