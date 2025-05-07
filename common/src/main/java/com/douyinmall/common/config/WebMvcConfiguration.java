package com.douyinmall.common.config;

import com.douyinmall.common.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

        private final JwtTokenUserInterceptor jwtTokenUserInterceptor;

        @Autowired
        public WebMvcConfiguration(JwtTokenUserInterceptor jwtTokenUserInterceptor) {
                this.jwtTokenUserInterceptor = jwtTokenUserInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                log.info("开始注册自定义拦截器...");
                registry.addInterceptor(jwtTokenUserInterceptor)
                        .addPathPatterns("/user/*")
                        .addPathPatterns("/shop/*")
                        .addPathPatterns("/address/*")
                        .addPathPatterns("/order/*")
                        .addPathPatterns("/cart/*")
                        .addPathPatterns("/promotion/**")
                        .excludePathPatterns("/user/login")
                        .excludePathPatterns("/user/register");
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 这里可以添加资源处理器的配置
        }
}