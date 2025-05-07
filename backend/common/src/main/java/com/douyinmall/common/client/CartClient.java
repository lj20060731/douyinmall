package com.douyinmall.common.client;

import com.douyinmall.common.entity.Cart;
import com.douyinmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.function.Function;

@FeignClient("cart-service")
public interface CartClient {
        @GetMapping("/cart/getcartbyid")
        Result<Cart> getCartById(@RequestParam("cartId") Long cartId);
        @GetMapping("/cart/deleteallcart")
        void deleteAllCart(@RequestParam("userId") Long userId);
        @GetMapping("/cart/getcartbyids")
        Result<List<Cart>> getCartByIds(@RequestParam("cartIds")List<Long> cartIds);
}
