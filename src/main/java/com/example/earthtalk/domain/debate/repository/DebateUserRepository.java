package com.example.earthtalk.domain.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.debate.entity.DebateUser;

public interface DebateUserRepository extends JpaRepository<DebateUser, Long> {
}
