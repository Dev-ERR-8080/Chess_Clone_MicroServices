package org.example.matchmaking_service.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchFoundDto {
    Long matchId;
    String color;
    String mode;
    String opponentName;
    String initialFen ;

}
