package com.example.earthtalk.config;


import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String rabbit_host;

    @Value("${spring.rabbitmq.username}")
    private String rabbit_username;

    @Value("${spring.rabbitmq.port}")
    private int rabbit_port;

    @Value("${spring.rabbitmq.password}")
    private String rabbit_password;

    public static final String EXCHANGE_NAME = "chat.exchange";
    public static final String QUEUE_PREFIX = "chat.queue.";
    public static final String KEY_PREFIX = "chat.key";

    public static final String DEBATE_SUFFIX = "debate";
    public static final String OBSERVER_SUFFIX = "observer";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbit_host);
        connectionFactory.setUsername(rabbit_username);
        connectionFactory.setPassword(rabbit_password);
        connectionFactory.setPort(rabbit_port);
        return connectionFactory;
    }

    @Bean
    public RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry() {
        return new RabbitListenerEndpointRegistry();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

}
