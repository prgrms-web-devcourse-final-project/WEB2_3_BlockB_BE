package com.example.earthtalk.domain.debate.repository;


import java.util.Optional;

import com.example.earthtalk.domain.debate.entity.DebateChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebateChatRepository extends JpaRepository<DebateChat, Long> {

}
