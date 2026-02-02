package org.example.game_engine_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTimeoutConfig {
    public static final String EXCHANGE = "game.commands.exchange";
    public static final String QUEUE = "game.timeout.queue";
    public static final String ROUTING_KEY = "game.timeout.key";

    @Bean
    public Queue timeoutQueue() { return new Queue(QUEUE); }
    @Bean public TopicExchange commandExchange() { return new TopicExchange(EXCHANGE); }
    @Bean public Binding timeoutBinding(Queue timeoutQueue, TopicExchange commandExchange) {
        return BindingBuilder.bind(timeoutQueue).to(commandExchange).with(ROUTING_KEY);
    }
}
