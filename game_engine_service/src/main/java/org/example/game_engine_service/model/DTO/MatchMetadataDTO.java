package org.example.game_engine_service.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchMetadataDTO {
    String whitePlayerId;
    String blackPlayerId;
    String status;
    String mode;
}
