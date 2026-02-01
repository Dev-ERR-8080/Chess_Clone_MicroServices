package org.example.game_engine_service.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchState implements Serializable {

    private String matchId;

    private Long whitePlayerId;
    private Long blackPlayerId;

    private String fen;              // Current board position
    private PieceColor turn;          // Who moves next
    private MatchStatus status;

    public boolean isPlayer(Long userId) {
        return userId.equals(whitePlayerId) || userId.equals(blackPlayerId);
    }

    public PieceColor getPlayerColor(Long userId) {
        if (userId.equals(whitePlayerId)) return PieceColor.WHITE;
        if (userId.equals(blackPlayerId)) return PieceColor.BLACK;
        throw new IllegalArgumentException("User not part of this match");
    }

    public void toggleTurn() {
        this.turn = this.turn.opposite();
    }

}
