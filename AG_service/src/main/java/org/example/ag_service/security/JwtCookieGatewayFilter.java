package org.example.ag_service.security;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtCookieGatewayFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtCookieGatewayFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        System.out.println("JWT Filter hit for path: "+path);
        // ✅ Public endpoints
        if (path.startsWith("/auth/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login")
                || path.startsWith("/ws")
                || path.startsWith("/ws/info")) {
            return chain.filter(exchange);
        }

        // 🍪 Extract JWT cookie
        HttpCookie jwtCookie = exchange.getRequest()
                .getCookies()
                .getFirst("JWT");

        if (jwtCookie == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            Claims claims = jwtUtil.extractClaims(jwtCookie.getValue());

            // 🔁 Propagate identity
            ServerHttpRequest mutatedRequest =
                    exchange.getRequest()
                            .mutate()
                            .header("X-User-Email", claims.getSubject())
                            .header("X-User-Role", claims.get("role").toString())
                            .header("X-User-Id", claims.get("userId").toString())
                            .build();
            System.out.println("Email: "+claims.getSubject());
            System.out.println("Role: "+claims.get("role").toString());
            System.out.println("UserID: "+claims.get("userId").toString());
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // BEFORE routing
    }
}
