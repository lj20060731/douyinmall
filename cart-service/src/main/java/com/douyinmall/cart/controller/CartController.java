package com.douyinmall.cart.controller;



import com.douyinmall.cart.domain.VO.Cart;
import com.douyinmall.cart.service.CartService;
import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.entity.Product;
import com.douyinmall.common.result.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
        @Autowired
        private CartService cartService;
        @Autowired
        private TokenHolder tokenHolder;

        @PostMapping("/addcart")
        public Result<String> addCart(@RequestParam("productId") Long productId) {
                // 从线程中获取用户id
                Long currentId = tokenHolder.getUserId();
                Product product = cartService.getProductById(productId);
                if (product == null) {
                        // 商品不存在，返回错误信息
                        return Result.error("商品不存在");
                }
                // 查询当前用户的购物车中是否已经存在该商品
                Cart cart = cartService.getCartByUserIdAndProductId(currentId, productId);
                if (cart != null) {
                        // 如果存在，数量加1
                        cart.setNumber(cart.getNumber() + 1);
                        cart.setAmount(cart.getAmount() + product.getPrice());
                        cartService.updateCart(cart);
                } else {
                        cart = new Cart();
                        BeanUtils.copyProperties(product, cart);
                        cart.setUserId(currentId);
                        cart.setNumber(1);
                        cart.setProductId(productId);
                        cart.setAmount(product.getPrice());
                        cart.setCreateTime(LocalDateTime.now());
                        cart.setProductType(product.getProductType());
                        cart.setPrice(product.getPrice());
                        cartService.addCart(cart);
                }
                // 创建一个包含成功信息的 Map 对象
                Map<String, String> resultMap = new HashMap<>();
               return Result.success("操作成功");
        }

        @PostMapping("/subcart")
        public Result<String> subCart(@RequestParam("productId") Long productId) {
                // 从线程中获取用户id
                Long currentId = tokenHolder.getUserId();
                Product product = cartService.getProductById(productId);
                if (product == null) {
                        // 商品不存在，返回错误信息
                        return Result.error("商品不存在");
                }
                // 查询当前用户的购物车中是否已经存在该商品
                Cart cart = cartService.getCartByUserIdAndProductId(currentId, productId);
                if (cart != null) {
                        if (cart.getNumber() == 1) {
                                // 如果数量为1，删除该商品
                                cartService.deleteCart(cart.getId());
                                return Result.success("操作成功");
                        } else {
                                // 如果存在，数量减1
                                cart.setNumber(cart.getNumber() - 1);
                                cart.setAmount(cart.getAmount() - product.getPrice());
                                cartService.updateCart(cart);
                                return Result.success("操作成功");
                        }
                } else {
                        return Result.error("商品不存在");
                }
        }

        @GetMapping("/getcart")
        public Result<List<Cart>> getCart() {
                Long currentId = tokenHolder.getUserId();
                List<Cart> cartList = cartService.getCartByUserId(currentId);
                return Result.success(cartList);
        }

        @GetMapping("/getcartbyid")
        public Result<Object> getCartById(@RequestParam Long cartId) {
                return Result.success(cartService.getCartById(cartId));
        }

        @GetMapping("/deleteallcart")
        public void deleteAllCart(){
                Long userId= tokenHolder.getUserId();
                cartService.deleteAllCart(userId);
        }

        @GetMapping("/getcartbyids")
        Result<List<Cart>> getCartByIds(@RequestParam(value = "cartIds") List<Long> cartIds){
                List<Cart> carts=cartService.listByIds(cartIds);
                return  Result.success(carts);
        }
}
