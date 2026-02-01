package org.example.timer_referee_service.controller;

import org.example.DTO.TimerInitDTO;
import org.example.DTO.TurnSwitchDTO;
import org.example.timer_referee_service.service.TimerRefereeService;
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
    public void startTimer(@RequestBody TimerInitDTO dto) {
        timerService.initializeTimer(dto.matchId(), dto.gameType());
    }

    @PostMapping("/switch")
    public void switchTurn(@RequestBody TurnSwitchDTO dto) {
        timerService.handleTurnSwitch(dto.matchId(), dto.nextTurn());
    }

}
