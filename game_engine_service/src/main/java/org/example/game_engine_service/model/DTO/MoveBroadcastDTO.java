package org.example.game_engine_service.model.DTO;

public record MoveBroadcastDTO(
        String matchId,
        String uci,
        String san,
        String fen,
        String nextTurn
) {
    @Override
    public String matchId() {
        return matchId;
    }

    @Override
    public String uci() {
        return uci;
    }

    @Override
    public String san() {
        return san;
    }

    @Override
    public String fen() {
        return fen;
    }

    @Override
    public String nextTurn() {
        return nextTurn;
    }
}
