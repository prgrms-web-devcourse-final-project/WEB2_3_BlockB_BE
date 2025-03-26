package com.example.earthtalk.domain.debate.service;

import com.example.earthtalk.config.RabbitMQConfig;
import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final SimpleRabbitListenerContainerFactory containerFactory;
    private final Map<String, SimpleMessageListenerContainer> listenerContainers = new ConcurrentHashMap<>();

    public void addListener(String roomId, String suffix) {
        String queueName = RabbitMQConfig.QUEUE_PREFIX + roomId + "_" + suffix;
        if (listenerContainers.containsKey(queueName)) {
            return;
        }

        SimpleMessageListenerContainer debateContainer = containerFactory.createListenerContainer();
        debateContainer.setQueueNames(queueName);
        debateContainer.setMessageListener(message -> {
            try {
                if(suffix.equals("debate")) {
                    DebateMessage debateMessage = convertMsg(message.getBody(), DebateMessage.class);
                    messagingTemplate.convertAndSend("/topic/" + suffix + "/" + roomId, debateMessage);
                } else {
                    ObserverMessage observerMessage = convertMsg(message.getBody(), ObserverMessage.class);
                    messagingTemplate.convertAndSend("/topic/" + suffix + "/" + roomId, observerMessage);
                }
                log.info("메시지 수신 및 웹소켓 전송 : roomId={}", roomId);
            } catch (IOException e) {
                log.info("메시지 변환 실패 : " + e.getMessage());
            }
        });

        debateContainer.start();
        listenerContainers.put(queueName, debateContainer);
    }

    public void removeListener(String roomId, String suffix) {
        String queueName = RabbitMQConfig.QUEUE_PREFIX + roomId + "_" + suffix;
        SimpleMessageListenerContainer container = listenerContainers.remove(queueName);
        if (container != null) {
            container.stop();
            log.info("메시지 리스너 중지 {}", queueName);
        }
    }

    public  <T> T convertMsg(byte[] messageBody, Class<T> clazz) throws IOException {
        return objectMapper.readValue(messageBody, clazz);
    }
}
