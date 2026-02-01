package org.example.iam_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.DTO.UserResponse;
import org.example.iam_service.model.DTO.LoginRequest;
import org.example.iam_service.model.DTO.RegisterRequest;
import org.example.iam_service.service.AuthService;
import org.example.iam_service.utils.JwtCookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth/") // for development only
public class AuthController {

    private final AuthService authService;
    private final JwtCookieUtil jwtCookieUtil;

    public AuthController(AuthService authService, JwtCookieUtil jwtCookieUtil) {
        this.authService = authService;
        this.jwtCookieUtil = jwtCookieUtil;
    }



    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest registerDto
    ) {
        authService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest loginDto,
            HttpServletResponse response
    ) {
        System.out.println("request of login reached the auth controller with DTO: "+loginDto);
        String token = authService.login(loginDto);

        response.addHeader(
                "Set-Cookie",
                jwtCookieUtil.createJwtCookieHeader(token, false) // false = dev
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid  HttpServletResponse response) {
        response.addHeader(
                "Set-Cookie",
                jwtCookieUtil.clearJwtCookieHeader(false)
        );
        return ResponseEntity.ok().build();
    }
    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Auth is working properly");
    }
}
