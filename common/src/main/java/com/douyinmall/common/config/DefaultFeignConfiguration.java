package com.douyinmall.common.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


public class DefaultFeignConfiguration {
        @Bean
        public Logger.Level feignLoggerLevel() {
                return Logger.Level.FULL;
        }
}
