package com.example.earthtalk.domain.debate.service;

import com.example.earthtalk.config.RabbitMQConfig;
import com.example.earthtalk.domain.chat.repository.ObserverChatRepository;
import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQService {

    private final RabbitAdmin rabbitAdmin;
    private final DirectExchange exchange;
    private final RabbitMQListener listener;
    private final RabbitTemplate rabbitTemplate;

    public void bindRabbitMQ(String roomId) {
        rabbitAdmin.declareExchange(exchange);

        Queue debateQueue = createQueue(roomId, RabbitMQConfig.DEBATE_SUFFIX);
        binding(debateQueue, exchange, roomId, RabbitMQConfig.DEBATE_SUFFIX);
        listener.addListener(roomId, RabbitMQConfig.DEBATE_SUFFIX);

        Queue observerQueue = createQueue(roomId, RabbitMQConfig.OBSERVER_SUFFIX);
        binding(observerQueue, exchange, roomId, RabbitMQConfig.OBSERVER_SUFFIX);
        listener.addListener(roomId, RabbitMQConfig.OBSERVER_SUFFIX);

        log.info("RabbitMQ 설정 완료 : ${}", roomId);
    }

    public void deleteRabbitMq(String roomId) {
        String debateQueueName = RabbitMQConfig.QUEUE_PREFIX + roomId + "_" + RabbitMQConfig.DEBATE_SUFFIX;
        String observerQueueName = RabbitMQConfig.QUEUE_PREFIX + roomId + "_" + RabbitMQConfig.OBSERVER_SUFFIX;

        // 채팅을 저장하는 로직이 필요함.


        listener.removeListener(roomId, RabbitMQConfig.DEBATE_SUFFIX);
        listener.removeListener(roomId, RabbitMQConfig.OBSERVER_SUFFIX);
        rabbitAdmin.deleteQueue(debateQueueName);
        rabbitAdmin.deleteQueue(observerQueueName);
    }

    private Queue createQueue(String roomId, String suffix) {
        String rabbitQueue = RabbitMQConfig.QUEUE_PREFIX + roomId + "_" + suffix;
        Queue queue = new Queue(rabbitQueue, true);
        rabbitAdmin.declareQueue(queue);
        return queue;
    }

    private void binding(Queue queue, DirectExchange exchange, String roomId, String suffix) {
        String rabbitKey = RabbitMQConfig.KEY_PREFIX + roomId + "_" + suffix;
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(rabbitKey);
        rabbitAdmin.declareBinding(binding);
    }

    // 메시지를 RabbitMQ 에 전송 - 토론방
    public void sendRabbitMq(String roomId, DebateMessage message) {
        String rabbitKey = RabbitMQConfig.KEY_PREFIX + roomId + "_" + RabbitMQConfig.DEBATE_SUFFIX;
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, rabbitKey, message);
    }

    // 메시지를 RabbitMQ 에 전송 - 관전방
    public void sendRabbitMq(String roomId, ObserverMessage message) {
        String rabbitKey = RabbitMQConfig.KEY_PREFIX + roomId + "_" + RabbitMQConfig.OBSERVER_SUFFIX;
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, rabbitKey, message);
    }
}
