package org.example.matchmaking_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange matchExchange() {
        return new TopicExchange("match.found.exchange");
    }

    @Bean
    public Queue matchQueue() {
        return new Queue("match.found.queue");
    }

    @Bean
    public Binding matchBinding() {
        return BindingBuilder
                .bind(matchQueue())
                .to(matchExchange())
                .with("match.found");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public MessageConverter JacksonJsonMessageConverter(){
        return new JacksonJsonMessageConverter();
    }
}
