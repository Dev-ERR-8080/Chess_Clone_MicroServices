package org.example.user_profile_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue registrationQueue() {
        // name: user.registration.queue, durable: true
        return new Queue("user.registration.queue", true);
    }
    @Bean
    public MessageConverter jacksonConverter() {
        return new JacksonJsonMessageConverter();
    }
}