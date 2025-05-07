package com.douyinmall.common.config;

import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.interceptor.FeignTokenInterceptor;
import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FeignConfiguration.class);

    @Bean
    public RequestInterceptor feignTokenInterceptor(TokenHolder tokenHolder) {
        log.info("FeignTokenInterceptor bean created.");
        return new FeignTokenInterceptor(tokenHolder);
    }
}