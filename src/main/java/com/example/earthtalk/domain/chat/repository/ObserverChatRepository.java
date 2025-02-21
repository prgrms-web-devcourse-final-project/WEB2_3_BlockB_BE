package com.example.earthtalk.domain.chat.repository;

import com.example.earthtalk.domain.chat.ObserverChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObserverChatRepository extends JpaRepository<ObserverChat, Long> {
}
