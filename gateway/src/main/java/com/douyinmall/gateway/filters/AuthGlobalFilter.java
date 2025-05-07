package com.douyinmall.gateway.filters;

import com.douyinmall.common.properties.JwtProperties;
import com.douyinmall.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

//@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

       //@Autowired
        private JwtProperties jwtProperties;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                if (isExcludePath(request.getURI().getPath())) {
                        return chain.filter(exchange);
                }
                String authorizationHeader = request.getHeaders().getFirst(jwtProperties.getUserTokenName());
                String token = null;
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        token = authorizationHeader.substring(7);
                }
                try {
                        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                        Long userId = Long.valueOf(claims.get("userId").toString());
                } catch (Exception ex) {
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return response.setComplete();
                }
                System.out.println(":" + token);
                return chain.filter(exchange);
        }

        private boolean isExcludePath(String requestURI) {
                List<String> paths = new ArrayList<>();
                AntPathMatcher antPathMatcher = new AntPathMatcher();
                paths.add("/user/login");
                paths.add("/user/register");
                for (String path : paths) {
                        if (antPathMatcher.match(path, requestURI)) {
                                return true;
                        }
                }
                return false;
        }

        @Override
        public int getOrder() {
                return 0;
        }
}