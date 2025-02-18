package com.example.earthtalk.domain.debate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.debate.entity.DebateChat;

public interface DebateChatRepository extends JpaRepository<DebateChat, Long> {

}
