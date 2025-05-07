package com.douyinmall.common.interceptor;

import com.douyinmall.common.Holder.TokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeignTokenInterceptor implements RequestInterceptor {

        private static final Logger log = LoggerFactory.getLogger(FeignTokenInterceptor.class);
        private final TokenHolder tokenHolder;

        public FeignTokenInterceptor(TokenHolder tokenHolder) {
                this.tokenHolder = tokenHolder;
        }

        @Override
        public void apply(RequestTemplate template) {
                String token = tokenHolder.getToken();
                if (token != null) {
                        template.header("Authorization", "Bearer " + token);
                        log.info("Added Authorization token to Feign request: {}", token);
                } else {
                        log.warn("No token found in TokenHolder, Authorization header not added.");
                }
        }
}