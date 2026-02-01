package org.example.game_engine_service.model.DTO;


public record MoveMessageDTO(
        String matchId,
        String uci,
        String san,
        String fenBefore,
        String fenAfter,
        Long moveTimeMs,
        String promotion,
        int fromRow,
        int fromCol,
        int toRow,
        int toCol
) {
//    @Override
//    public String matchId() {
//        return matchId;
//    }
//
//    @Override
//    public String uci() {
//        return uci;
//    }
//
//    @Override
//    public String san() {
//        return san;
//    }
//
//    @Override
//    public String fenBefore() {
//        return fenBefore;
//    }
//
//    @Override
//    public String fenAfter() {
//        return fenAfter;
//    }
//
//    @Override
//    public long moveTimeMs() {
//        return moveTimeMs;
//    }
//
//    @Override
//    public int fromRow() {
//        return fromRow;
//    }
//
//    @Override
//    public int fromCol() {
//        return fromCol;
//    }
//
//    @Override
//    public int toRow() {
//        return toRow;
//    }
//
//    @Override
//    public int toCol() {
//        return toCol;
//    }
}