package org.example.iam_service.service;

import org.example.DTO.UserRegistrationEvent;
import org.example.iam_service.config.RabbitMQConfig;
import org.example.iam_service.model.User;
import org.example.iam_service.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class OAuthUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    public OAuthUserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
    }

    public User findOrCreateOAuthUser(String email, String name, String picture) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("OAUTH_USER"));
                    user.setRole("USER");
                    user.setProvider("GOOGLE");
                    User savedUser =(User) userRepository.save(user);

                    // rabbitmq message to user-profile about new user
                    UserRegistrationEvent event = new UserRegistrationEvent(
                            savedUser.getId(), email, name ,"INDIA", name, picture //INDIA IS SET FOR TESTING FIND A WAY TO GET THE ORIGINAL ORIGIN COUNTRY OF THE USER.
                    );
                    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
                    return savedUser;
                });
    }
}
