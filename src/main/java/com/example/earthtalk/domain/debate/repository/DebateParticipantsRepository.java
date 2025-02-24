package com.example.earthtalk.domain.debate.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.debate.entity.DebateParticipants;

public interface DebateParticipantsRepository extends JpaRepository<DebateParticipants, Long> {
	Optional<DebateParticipants> findByUser_Nickname(String username);
	Optional<DebateParticipants> findByDebate_UuidAndUser_Nickname(UUID debateUuid, String username);
	Optional<DebateParticipants> findByDebate_Uuid(UUID debateUuid);
}
