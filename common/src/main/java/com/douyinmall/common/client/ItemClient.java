package com.douyinmall.common.client;

import com.douyinmall.common.entity.Product;
import com.douyinmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("item-service")
public interface ItemClient {
        @GetMapping("/shop/getproductbyid")
        Result<Product> getProductById(@RequestParam("productId") Long productId);
}