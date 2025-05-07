package com.douyinmall.common.interceptor;

import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.properties.JwtProperties;
import com.douyinmall.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {
        @Autowired
        private JwtProperties jwtProperties;
        @Autowired
        private TokenHolder tokenHolder;

        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (jwtProperties == null) {
                        log.error("JwtProperties is null!");
                }
                //判断当前拦截到的是Controller的方法还是其他资源
                if (!(handler instanceof HandlerMethod)) {
                        //当前拦截到的不是动态方法，直接放行
                        return true;
                }
                //1、从请求头中获取令牌
                String authorizationHeader = request.getHeader(jwtProperties.getUserTokenName());
                String token = null;
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        token = authorizationHeader.substring(7);
                }

                //2、校验令牌
                try {
                        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                        Long userId = Long.valueOf(claims.get("userId").toString());
                        log.info("当前用户id：{}", userId);
                        tokenHolder.setUserId(userId);
                        tokenHolder.setToken(token);
                        //3、通过，放行
                        //BaseContext.setCurrentId(userId);
                        return true;
                } catch (Exception ex) {
                        //4、不通过，响应401状态码
                        response.setStatus(401);
                        return false;
                }
        }
}
