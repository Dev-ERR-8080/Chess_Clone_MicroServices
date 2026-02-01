package org.example.matchmaking_service.controller;

import org.example.matchmaking_service.model.DTO.MatchmakingRequest;
import org.example.DTO.GameType;
import org.example.matchmaking_service.service.MatchmakingService;
import org.example.matchmaking_service.service.UserProfileClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/matchmaking")
public class MatchmakingController {

    private final MatchmakingService matchmakingService;
    private final UserProfileClient userProfileClient;

    public MatchmakingController(MatchmakingService matchmakingService, UserProfileClient userProfileClient) {
        this.matchmakingService = matchmakingService;
        this.userProfileClient = userProfileClient;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody MatchmakingRequest req
    ) {

        System.out.println("Matchmaking start: user=" + userId + ", mode=" + req.getMode());

        int rating = userProfileClient.getRating(userId);

        boolean matched = matchmakingService.joinQueue(
                userId,
                rating,
                GameType.valueOf(req.getMode().toUpperCase())
        );

        return matched
                ? ResponseEntity.ok(Map.of("status", "MATCH_FOUND"))
                : ResponseEntity.ok(Map.of("status", "WAITING"));
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(
            @RequestHeader("X-User-Id") Long userId
    ) {
        matchmakingService.cancel(userId);
        return ResponseEntity.ok("CANCELLED");
    }
}
