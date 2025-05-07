//package com.douyinmall.common.utils;
//
//import com.douyinmall.common.interceptor.RestTemplateInterceptor;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Collections;
//
//public class RestTemplateUtil {
//    public static RestTemplate createRestTemplateWithToken(String token) {
//        RestTemplate restTemplate = new RestTemplate();
//        // 创建拦截器实例
//        RestTemplateInterceptor interceptor = new RestTemplateInterceptor(token);
//        // 将拦截器添加到 RestTemplate 中
//        restTemplate.setInterceptors(Collections.singletonList(interceptor));
//        return restTemplate;
//    }
//}