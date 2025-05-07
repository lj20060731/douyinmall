package com.douyinmall.user.controller;



import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.properties.JwtProperties;
import com.douyinmall.common.result.Result;
import com.douyinmall.common.utils.JwtUtil;
import com.douyinmall.user.domain.DTO.UserLogin;
import com.douyinmall.user.domain.DTO.UserRegister;
import com.douyinmall.user.domain.DTO.UserUpdate;
import com.douyinmall.user.domain.VO.User;
import com.douyinmall.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserController {
        @Autowired
        private UserService userService;
        @Autowired
        private JwtProperties jwtProperties;
        @Autowired
        private TokenHolder tokenHolder;

        @PostMapping("/login")
        public Result<Map<String, String>> login(@RequestBody UserLogin userLogin) {
                User user = userService.login(userLogin);
                if (user == null) {
                        // 登录失败，可以返回一个错误消息和相应的HTTP状态码
                        Map<String, String> errorMessage = new HashMap<>();
                        errorMessage.put("message", "账号或密码错误");
                        return Result.error(errorMessage);
                } else {
                        // 登录成功后，生成JWT令牌
                        Map<String, Object> claims = new HashMap<>();
                        claims.put("userId", user.getId());
                        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
                        Map<String, String> responseMap = new HashMap<>();
                        responseMap.put("token", token);
                        responseMap.put("username", user.getUsername());
                        responseMap.put("userId", String.valueOf(user.getId()));
                        return Result.success(responseMap);
                }
        }

        @GetMapping("/getInfo")
        public Result<Map<String, Object>> getInfo() {
                Long id = tokenHolder.getUserId();
                User user = userService.getInfo(id);
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("user", user);
                return Result.success(responseMap);
        }

        @PostMapping("/register")
        public Result<Map<String, String>> register(@RequestBody UserRegister userRegister) {
                User user = new User();
                BeanUtils.copyProperties(userRegister, user);
                user.setCreateTime(LocalDateTime.now());
                user.setStatus(1);
                user.setUpdateTime(LocalDateTime.now());
                userService.register(user);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "注册成功");
                return Result.success(responseMap);
        }

        @PostMapping("/update")
        public Result<Map<String, String>> update(@RequestBody UserUpdate userUpdate) {
                try {
                        Long id = tokenHolder.getUserId();
                        User user = new User();
                        BeanUtils.copyProperties(userUpdate, user);
                        user.setId(id);
                        user.setStatus(1);
                        user.setUpdateTime(LocalDateTime.now());
                        userService.update(user);
                        Map<String, String> responseMap = new HashMap<>();
                        responseMap.put("message", "更新成功");
                        return Result.success(responseMap);
                } catch (Exception e) {
                        Map<String, String> responseMap = new HashMap<>();
                        responseMap.put("message", "更新失败");
                        return Result.error(responseMap);
                }

        }
}

