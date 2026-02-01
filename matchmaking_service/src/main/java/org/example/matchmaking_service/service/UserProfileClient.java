package org.example.matchmaking_service.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="USER-PROFILE-SERVICE")
public interface UserProfileClient {

    @GetMapping("/internal/users/{userId}/rating")
    Integer getRating(@PathVariable("userId") Long userId);

}
