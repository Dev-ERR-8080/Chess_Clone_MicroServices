package org.example.game_engine_service.service;

import org.example.game_engine_service.model.DTO.RedisMoveEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MoveEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public MoveEventPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    private String channel(String matchId) {
        return "match:" + matchId;
    }

    public void publish(RedisMoveEvent event) {
        String channel = channel(event.getMatchId());
        redisTemplate.convertAndSend(channel, event);
    }

}
