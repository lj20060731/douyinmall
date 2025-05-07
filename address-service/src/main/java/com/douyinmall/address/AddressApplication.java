package com.douyinmall.address;


import com.douyinmall.common.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.douyinmall.address.mapper")
@ComponentScan(basePackages = {"com.douyinmall.common", "com.douyinmall.address"}, excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.douyinmall.common.config.MqConfiguration.class
        )
})
@EnableCaching
public class AddressApplication{

        public static void main(String[] args) {
                SpringApplication.run(AddressApplication.class, args);
                log.info("Service Startup Successful!");
        }
}
