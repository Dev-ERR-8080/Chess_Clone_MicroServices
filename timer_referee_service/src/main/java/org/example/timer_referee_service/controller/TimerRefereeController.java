package org.example.timer_referee_service.controller;

import org.example.DTO.TimerInitDTO;
import org.example.DTO.TurnSwitchDTO;
import org.example.timer_referee_service.service.TimerRefereeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/timer")
@RestController
public class TimerRefereeController {
    private TimerRefereeService timerService;

    public TimerRefereeController(TimerRefereeService timerService) {
        this.timerService = timerService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startTimer(@RequestBody TimerInitDTO dto) {
        if(dto.matchId() == null || dto.gameType()== null){
            return ResponseEntity.badRequest().body("MatchId and GameType are required");
        }
        timerService.initializeTimer(dto.matchId(), dto.gameType());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/switch")
    public ResponseEntity<?> switchTurn(@RequestBody TurnSwitchDTO dto) {
        if(dto.matchId() == null || dto.nextTurn() == null) {
            return ResponseEntity.badRequest().body("MatchId and NextTurn are required");
        }
        timerService.handleTurnSwitch(dto.matchId(), dto.nextTurn());
        return ResponseEntity.ok().build();
    }

}
