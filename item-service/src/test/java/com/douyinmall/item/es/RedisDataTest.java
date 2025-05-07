//package com.douyinmall.item.es;
//
//import cn.hutool.json.JSONUtil;
//import com.douyinmall.item.domain.VO.Product;
//import com.douyinmall.item.service.ShopService;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import javax.annotation.Resource;
//
//public class RedisDataTest {
//        @Resource
//        private StringRedisTemplate stringRedisTemplate;
//        @Resource
//        private ShopService shopService;
//        @Test
//        void data(){
//                for (long i=1;i<=10;i++){
//                        Product product=shopService.getProductById(i);
//                        stringRedisTemplate.opsForValue().set("shop:"+i, JSONUtil.toJsonStr(product));
//                }
//        }
//
//}
