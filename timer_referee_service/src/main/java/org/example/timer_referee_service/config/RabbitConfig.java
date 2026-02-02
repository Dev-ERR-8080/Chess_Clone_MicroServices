package org.example.timer_referee_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;

public class RabbitConfig {
    public static final String MATCH_RESULT_QUEUE = "match.found.exchange";

    @Bean
    public Queue matchResultQueue() {
        return QueueBuilder.durable(MATCH_RESULT_QUEUE).build();
    }

    @Bean
    public JacksonJsonMessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }
}
