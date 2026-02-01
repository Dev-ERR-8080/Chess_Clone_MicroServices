package org.example.iam_service.utils;

import org.springframework.stereotype.Component;

@Component
public class JwtCookieUtil {

    public String createJwtCookieHeader(String token, boolean isProd) {
        if (isProd) {
            return "JWT=" + token +
                    "; Path=/; HttpOnly; Secure; SameSite=Strict";
        }
        return "JWT=" + token +
                "; Path=/; HttpOnly; SameSite=Lax";
    }

    public String clearJwtCookieHeader(boolean isProd) {
        if (isProd) {
            return "JWT=; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=0";
        }
        return "JWT=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0";
    }
}
