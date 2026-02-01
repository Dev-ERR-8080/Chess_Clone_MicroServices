package org.example.matchmaking_service.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.DTO.GameType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchmakingResponse {
    private Long matchId;
    private GameType mode;
    private String color;
    private String opponentUsername;
    private LocalDateTime startTime;
}

