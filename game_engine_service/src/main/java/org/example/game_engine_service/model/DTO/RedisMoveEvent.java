package org.example.game_engine_service.model.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisMoveEvent implements Serializable {

    private String matchId;
    private String uci;
    private String fen;
    private Long playerId;

}
