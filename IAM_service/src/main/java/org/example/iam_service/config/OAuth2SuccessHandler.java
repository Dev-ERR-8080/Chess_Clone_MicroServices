package org.example.iam_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.iam_service.model.User;
import org.example.iam_service.model.UserPrincipal;
import org.example.iam_service.service.OAuthUserService;
import org.example.iam_service.utils.JwtCookieUtil;
import org.example.iam_service.utils.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final OAuthUserService oAuthUserService;
    private final JwtCookieUtil jwtCookieUtil;

    public OAuth2SuccessHandler(JwtUtil jwtUtil,
                                UserDetailsService userDetailsService,
                                OAuthUserService oAuthUserService, JwtCookieUtil jwtCookieUtil) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.oAuthUserService = oAuthUserService;
        this.jwtCookieUtil = jwtCookieUtil;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        System.out.println(oAuth2User.getAttributes());

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        User user = oAuthUserService.findOrCreateOAuthUser(email,name,picture);

        UserPrincipal principal = new UserPrincipal(user);

        String token = jwtUtil.generateToken(principal);

        response.addHeader(
                "Set-Cookie",
                jwtCookieUtil.createJwtCookieHeader(token,false)
        );

        response.sendRedirect("http://localhost:3000/home");

    }
}
