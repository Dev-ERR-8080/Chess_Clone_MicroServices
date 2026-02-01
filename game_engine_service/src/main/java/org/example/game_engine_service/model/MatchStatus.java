package org.example.game_engine_service.model;


import java.util.Arrays;

public enum MatchStatus {
    PLAYER1_WON(-1),
    DRAW(2),
    ONGOING(0),
    PLAYER2_WON(1),
    FINISHED(-1);

    private final int code;

    MatchStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MatchStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid GameStatus code: " + code));
    }
}
