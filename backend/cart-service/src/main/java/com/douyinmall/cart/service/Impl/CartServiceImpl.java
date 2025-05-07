package com.douyinmall.cart.service.Impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.douyinmall.cart.domain.VO.Cart;
import com.douyinmall.cart.mapper.CartMapper;
import com.douyinmall.cart.service.CartService;
import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.client.ItemClient;
import com.douyinmall.common.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

        private final CartMapper cartMapper;

        private final DiscoveryClient discoveryClient;

        private final TokenHolder tokenHolder;

        private final ItemClient itemClient;

        @Override
        public void addCart(Cart cart) {
                cartMapper.insertCart(cart);
        }

        @Override
        public Cart getCartByUserIdAndProductId(Long currentId, Long productId) {
                return cartMapper.getCartByUserIdAndProductId(currentId, productId);
        }

        @Override
        public void updateCart(Cart cart) {
                cartMapper.updateCart(cart);
        }

        @Override
        public void deleteCart(Long id) {
                cartMapper.deleteCart(id);
        }

        @Override
        public List<Cart> getCartByUserId(Long currentId) {
                return cartMapper.getCartByUserId(currentId);
        }

        //@GlobalTransactional
        @Override
        public Product getProductById(Long productId) {
                return itemClient.getProductById(productId).getData();
        }

        @Override
        public Object getCartById(Long cartId) {
                return cartMapper.getById(cartId);
        }

        @Override
        public void deleteAllCart(Long userId) {
                cartMapper.deleteAllCart(userId);
        }
}
