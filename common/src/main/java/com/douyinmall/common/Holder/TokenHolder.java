package com.douyinmall.common.Holder;

import org.springframework.stereotype.Component;

@Component
public class TokenHolder {

        private Long userId;
        private String token;

        public synchronized String getToken() {
                return token;
        }

        public synchronized Long getUserId() {
                return userId;
        }

        public synchronized void setUserId(Long userId) {
                this.userId = userId;
        }

        public synchronized void setToken(String token) {
                this.token = token;
        }

        public synchronized void clear() {
                this.userId = null;
                this.token = null;
        }
}