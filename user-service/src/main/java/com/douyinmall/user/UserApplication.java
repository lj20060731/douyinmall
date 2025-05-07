package com.douyinmall.user;


import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.douyinmall.common", "com.douyinmall.user"}, excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.douyinmall.common.config.MqConfiguration.class
        )
})
@EnableTransactionManagement
@MapperScan("com.douyinmall.user.mapper")
@EnableCaching
@EnableFeignClients
public class UserApplication {

        public static void main(String[] args) {
                SpringApplication.run(UserApplication.class, args);
                log.info("Service Startup Successful!");
        }
}
