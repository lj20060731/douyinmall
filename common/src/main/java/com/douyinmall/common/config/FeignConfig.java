package com.douyinmall.common.config;

import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Connection", "keep-alive");
            template.header("Keep-Alive", "timeout=60");
        };
    }
}