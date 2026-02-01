package org.example.user_profile_service.controller;


import org.example.user_profile_service.model.UserProfile;
import org.example.user_profile_service.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserProfileController {


    private UserProfileService userService;

    public UserProfileController(UserProfileService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile(@RequestHeader("X-User-Id") Long userId) {
        System.out.println("user profile end point is called contains : \n" +"User Id: "+userId+"\n");
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/rating")
    public ResponseEntity<UserProfile> updateRating(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Integer rating
    ) {
        System.out.println("Rating is called");
        return ResponseEntity.ok(userService.updateRating(userId, rating));
    }

    @GetMapping("/internal/{userId}/rating")
    public Integer getRating(@PathVariable Long userId) {
        return userService.getRating(userId);
    }

}
