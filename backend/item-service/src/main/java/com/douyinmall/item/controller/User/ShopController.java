package com.douyinmall.item.controller.User;



import com.douyinmall.common.result.PageResult;
import com.douyinmall.common.result.Result;
import com.douyinmall.item.domain.DTO.ProductPageQuery;

import com.douyinmall.item.domain.VO.Product;
import com.douyinmall.item.service.ShopService;

import lombok.AllArgsConstructor;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController("userShopController")
@RequestMapping(value = "/shop")
@AllArgsConstructor
public class ShopController {
        private final ShopService shopService;
        private final ZhiPuAiChatModel chatModel;

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

        @GetMapping("/searchproduct")
        public Result<List<Product>> searchProduct(@RequestParam String description) {
                try {
                        List<Product> productList = shopService.searchProduct(description);
                        return Result.success(productList);
                } catch (IOException e) {
                        return Result.error(new ArrayList<>());
                }
        }

        @GetMapping("/generate")
        public Result<String> generate(@RequestParam(value = "message", defaultValue = "hello") String message) {
                String s=this.chatModel.call("润色这段对于商品描述的话，便于进行elasticsearch的搜索，不要超过20个字，可以直接搜索的，不要一点废话，可以直接搜索，也不需要冒号，不需要解释，可以直接进行搜索，你给我的回答我可以直接进行搜索："+message);
                if (!s.isEmpty()){
                        return Result.success(s);
                }else {
                        return Result.error("出错了");
                }
        }
}
