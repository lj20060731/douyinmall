package com.douyinmall.cart;



import com.douyinmall.common.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.douyinmall.cart.mapper")
@EnableCaching
@ComponentScan(basePackages = {"com.douyinmall.common", "com.douyinmall.cart"})
@EnableFeignClients(basePackages = "com.douyinmall.common.client")
public class CartApplication{

        public static void main(String[] args) {
                SpringApplication.run(CartApplication.class, args);
                log.info("Service Startup Successful!");
        }
}
