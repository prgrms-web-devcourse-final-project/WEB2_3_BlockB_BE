package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateChat;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DebateChatManagementServiceTest {

	@Mock
	private DebateChatRepository debateChatRepository;

	@Mock
	private DebateUserService debateUserService;

	@Mock
	private DebateService debateService;

	@InjectMocks
	private DebateChatManagementService debateChatManagementService;

	private Debate mockDebate;
	private DebateParticipants mockDebateParticipants;
	private LocalDateTime now;

	@BeforeEach
	public void setUp() {
		mockDebate = mock(Debate.class);
		mockDebateParticipants = mock(DebateParticipants.class);
		now = LocalDateTime.now();
	}




}
