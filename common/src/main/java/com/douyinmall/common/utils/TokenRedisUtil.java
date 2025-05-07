//package com.douyinmall.common.utils;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//public class TokenRedisUtil {
//        /**
//         * 将 token 存储到 Redis 中并设置 1 小时的过期时间
//         *
//         * @param key   存储 token 的键
//         * @param token 要存储的 token
//         */
//        public void saveTokenWithExpiration(String key, String token) {
//                StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
//                // 将 token 存储到 Redis 中
//                stringRedisTemplate.opsForValue().set(key, token);
//                // 设置过期时间为 1 小时（3600 秒）
//                stringRedisTemplate.expire(key, 3600, TimeUnit.SECONDS);
//        }
//
//        /**
//         * 从 Redis 中获取 token
//         *
//         * @param key 存储 token 的键
//         * @return token 或 null（如果不存在或已过期）
//         */
//        public String getToken(String key) {
//                StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
//                return stringRedisTemplate.opsForValue().get(key);
//        }
//}