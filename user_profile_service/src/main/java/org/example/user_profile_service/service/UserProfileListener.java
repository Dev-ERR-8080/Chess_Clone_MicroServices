package org.example.user_profile_service.service;

import org.example.DTO.UserRegistrationEvent;
import org.example.user_profile_service.model.UserProfile;
import org.example.user_profile_service.repository.UserProfileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserProfileListener {

    private final UserProfileRepository profileRepository;

    public UserProfileListener(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @RabbitListener(queues = "user.registration.queue")
    public void handleUserRegistration(UserRegistrationEvent event) {
        System.out.println("Received registration for user ID: " + event.getUserId());

        UserProfile profile = new UserProfile();
        profile.setUserId(event.getUserId());
        profile.setFullName(event.getFullName());
        profile.setCountry(event.getCountry());
        profile.setCurrentRating(250);
        profile.setUsername(event.getUserName());
        profile.setPfpUrl(event.getPfpUrl() );
        profileRepository.save(profile);
    }

}
