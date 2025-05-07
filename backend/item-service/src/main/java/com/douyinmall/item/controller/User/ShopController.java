package com.douyinmall.item.controller.User;



import com.douyinmall.common.result.PageResult;
import com.douyinmall.common.result.Result;
import com.douyinmall.item.domain.DTO.ProductPageQuery;

import com.douyinmall.item.domain.VO.Product;
import com.douyinmall.item.service.ShopService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping(value = "/shop")
public class ShopController {
        @Autowired
        private ShopService shopService;

        //@Cacheable(cacheNames = "productListCache")
        @PostMapping("/getproductlist")
        public Result<PageResult> getProductList(@RequestBody ProductPageQuery query) {
                PageResult pageResult = shopService.getProductList(query);
                return Result.success(pageResult);
        }

        @Cacheable(cacheNames = "getProductsNumCache")
        @GetMapping("/getproductsnum")
        public Result<Integer> getProductsNum() {
                Integer num = shopService.getProductsNum();
                return Result.success(num);
        }

        @GetMapping("/getproductbyid")
        public Result<Product> getProductById(@RequestParam Long productId) {
                Product product = shopService.getProductById(productId);
                return Result.success(product);
        }
}
