package org.example.game_engine_service.controller;

import org.example.game_engine_service.model.DTO.MatchMetadataDTO;
import org.example.game_engine_service.service.GameStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/game")
public class GameRestController {

    private final GameStateService gameStateService;

    public GameRestController(GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }

    @GetMapping("/match/{matchId}/metadata")
    public ResponseEntity<MatchMetadataDTO> getMatchMetadata(@PathVariable String matchId) {
        // Fetches ONLY player1Id, player2Id, and gameMode from Redis Hash
        Map<Object, Object> state = gameStateService.getMatchState(matchId);

        return ResponseEntity.ok(new MatchMetadataDTO(
                (String) state.get("whitePlayerId"),
                (String) state.get("blackPlayerId"),
                (String) state.get("status"),
                (String) state.get("mode")
        ));
    }
}
