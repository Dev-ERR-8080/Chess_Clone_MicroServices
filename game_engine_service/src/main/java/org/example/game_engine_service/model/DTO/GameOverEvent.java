package org.example.game_engine_service.model.DTO;

public record GameOverEvent(
        String matchId,
        Long winnerId,
        String reason,
        String finalFen
) {}
