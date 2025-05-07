package com.douyinmall.cart.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.douyinmall.cart.domain.VO.Cart;
import com.douyinmall.common.entity.Product;

import java.util.List;

public interface CartService extends IService<Cart> {
        void addCart(Cart cart);

        Cart getCartByUserIdAndProductId(Long currentId, Long productId);

        void updateCart(Cart cart);

        void deleteCart(Long id);

        List<Cart> getCartByUserId(Long currentId);

        Product getProductById(Long productId);

        Object getCartById(Long cartId);

        void deleteAllCart(Long userId);
}
