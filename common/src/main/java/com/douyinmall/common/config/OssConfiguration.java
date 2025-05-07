package com.douyinmall.common.config;



import com.douyinmall.common.properties.AliOssProperties;
import com.douyinmall.common.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {
        @Autowired
        private AliOssProperties aliOssProperties;

        @Bean
        @ConditionalOnMissingBean
        //如果容器中没有这个bean的话，才会创建这个bean
        public AliOssUtil aliOssUtil(){
                log.info("开始加载阿里云配置...");
                return new AliOssUtil(aliOssProperties.getEndpoint(), aliOssProperties.getAccessKeyId(), aliOssProperties.getAccessKeySecret(), aliOssProperties.getBucketName());
        }
}
