package com.douyinmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

@ComponentScan(basePackages = {"com.douyinmall.common", "com.douyinmall.gateway"}, excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.douyinmall.common.config.MqConfiguration.class
        )
})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@PropertySource("classpath:application.properties")
public class GatewayApplication {
        public static void main(String[] args) {
                SpringApplication.run(GatewayApplication.class, args);
        }
}