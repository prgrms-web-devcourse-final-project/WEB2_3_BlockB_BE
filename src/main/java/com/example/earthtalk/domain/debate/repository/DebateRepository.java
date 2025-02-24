package com.example.earthtalk.domain.debate.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.debate.entity.Debate;

public interface DebateRepository extends JpaRepository<Debate, Long> {
	Optional<Debate> findByUuid(UUID uuid);
}
