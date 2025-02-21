package com.example.earthtalk.domain.debate.repository;

import com.example.earthtalk.domain.debate.entity.Debate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebateRepository extends JpaRepository<Debate, Long> {
}
