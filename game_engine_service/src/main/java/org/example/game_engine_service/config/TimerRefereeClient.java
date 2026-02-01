package org.example.game_engine_service.config;

import org.example.DTO.TimerInitDTO;
import org.example.DTO.TurnSwitchDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// "timer-referee-service" should match the name in Eureka/Application Properties
@FeignClient(name = "timer-referee-service", path = "/timer")
public interface TimerRefereeClient {

    @PostMapping("/start")
    void startTimer(@RequestBody TimerInitDTO dto);

    @PostMapping("/switch")
    void switchTurn(@RequestBody TurnSwitchDTO dto);
}
