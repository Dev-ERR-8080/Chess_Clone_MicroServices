package org.example.iam_service.service;

import org.example.DTO.UserRegistrationEvent;
import org.example.iam_service.config.RabbitMQConfig;
import org.example.iam_service.exceptionHandler.ApplicationExceptions;
import org.example.iam_service.model.DTO.LoginRequest;
import org.example.iam_service.model.DTO.RegisterRequest;
import org.example.iam_service.model.User;
import org.example.iam_service.model.UserPrincipal;
import org.example.iam_service.repository.UserRepository;
import org.example.iam_service.utils.JwtUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.rabbitTemplate=rabbitTemplate;
    }

    // ================= REGISTER =================
    public void register(RegisterRequest dto) {

        if (userRepository.findByEmail(dto.getUserEmailId()).isPresent()) {
            throw new ApplicationExceptions.EmailAlreadyExists("Email already exists. Try sign in");
        }

        User user = new User();
        user.setEmail(dto.getUserEmailId());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // server-controlled fields
        user.setRole("USER");
        user.setProvider("LOCAL");

        User savedUser = (User) userRepository.save(user);

        // rabbitmq message to user-profile about new user
        UserRegistrationEvent event = new UserRegistrationEvent(
                savedUser.getId(), dto.getUserEmailId(), dto.getUserFullName(), dto.getCountry(), dto.getUserName(), dto.getPfpUrl()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }


    // ================= LOGIN =================
    public String login(LoginRequest loginDto) {
//        System.out.println("DTO = " + loginDto);
//        System.out.println(loginDto.getUserEmailId()+" "+loginDto.getPassword());

        if (loginDto.getUserEmailId() == null || loginDto.getPassword() == null) {
            throw new ApplicationExceptions.BadRequest("Email and password are required");
        }
        Authentication authentication ;
        try{
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginDto.getUserEmailId(),
                                    loginDto.getPassword()
                            )
                    );
        }catch (Exception e) {
            throw new ApplicationExceptions.UserNotFound("Invalid Email or password");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        System.out.println(userPrincipal.getUser());
        return jwtUtil.generateToken(userPrincipal);
    }

}
