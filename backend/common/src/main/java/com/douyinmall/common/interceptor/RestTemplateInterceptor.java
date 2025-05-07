//package com.douyinmall.common.interceptor;
//
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpRequest;
//import org.springframework.http.client.ClientHttpRequestExecution;
//import org.springframework.http.client.ClientHttpRequestInterceptor;
//import org.springframework.http.client.ClientHttpResponse;
//
//import java.io.IOException;
//
//public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {
//
//        private final String token;
//
//        public RestTemplateInterceptor(String token) {
//                this.token = token;
//        }
//
//
//        @Override
//        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//                // 获取请求头
//                HttpHeaders headers = request.getHeaders();
//                // 添加 Authorization 请求头
//                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//                // 继续执行请求
//                return execution.execute(request, body);
//        }
//}