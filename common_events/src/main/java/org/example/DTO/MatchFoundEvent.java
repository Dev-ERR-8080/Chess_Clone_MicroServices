package org.example.DTO;

public record MatchFoundEvent(
        String matchId,
        Long player1Id,
        Long player2Id,
        GameType gameType
) {
    public String getMatchId() {
        return matchId;
    }

    public Long getPlayer1Id() {
        return player1Id;
    }

    public Long getPlayer2Id() {
        return player2Id;
    }

    public GameType getGameType() {
        return gameType;
    }
}
