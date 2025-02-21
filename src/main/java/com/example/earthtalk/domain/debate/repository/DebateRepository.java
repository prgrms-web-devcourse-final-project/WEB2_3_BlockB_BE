package com.example.earthtalk.domain.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.debate.entity.Debate;

public interface DebateRepository extends JpaRepository<Debate, Long> {
}
