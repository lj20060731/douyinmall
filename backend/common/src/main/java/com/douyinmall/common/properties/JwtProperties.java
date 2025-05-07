package com.douyinmall.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.InitializingBean;
import lombok.extern.slf4j.Slf4j;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
@Slf4j
public class JwtProperties implements InitializingBean {
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("JwtProperties 绑定结果: userSecretKey={}, userTtl={}, userTokenName={}",
                userSecretKey, userTtl, userTokenName);
    }
}