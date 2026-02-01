package org.example.user_profile_service.controller;

import org.example.user_profile_service.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/internal/users")
public class InternalUserClientController {

    private final UserProfileService userProfileService;

    public InternalUserClientController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{userId}/rating")
    public Integer getRating(@PathVariable("userId") Long userId) {
        System.out.println("rating controller hit with userId: "+userId);
        return userProfileService.getRating(userId);
    }
}
