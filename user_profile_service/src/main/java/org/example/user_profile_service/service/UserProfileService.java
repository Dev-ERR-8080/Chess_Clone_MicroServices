package org.example.user_profile_service.service;

import org.example.user_profile_service.expectionHandler.ApplicationExceptions;
import org.example.user_profile_service.model.UserProfile;
import org.example.user_profile_service.repository.UserProfileRepository;
import org.springframework.stereotype.Service;


@Service
public class UserProfileService {

    private UserProfileRepository userRepository;

    public UserProfileService(UserProfileRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfile getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationExceptions.UserNotFound("Profile not found"));
    }

    public UserProfile updateRating(Long userId, Integer newRating) {
        UserProfile user = userRepository.findById(userId).orElseThrow(()-> new ApplicationExceptions.UserNotFound("Profile not found"));
        user.setCurrentRating(newRating);
        return userRepository.save(user);
    }

    public Integer getRating(Long userId) {
        System.out.println("User service hit for rating of the user");
        UserProfile user =userRepository.findById(userId).orElseThrow(()-> new ApplicationExceptions.UserNotFound("User not found"));
        System.out.println("user found: "+user);
        return user.getCurrentRating();
    }
}
