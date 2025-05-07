package com.douyinmall.cart.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyinmall.cart.domain.VO.Cart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
        void insertCart(Cart cart);
        void updateCart(Cart cart);

        @Select("SELECT * FROM cart WHERE user_id = #{currentId} AND product_id = #{productId}")
        Cart getCartByUserIdAndProductId(Long currentId, Long productId);

        List<Cart> getCartByUserId(Long currentId);

        @Delete("DELETE FROM cart WHERE id = #{id}")
        void deleteCart(Long id);

        @Select("SELECT * FROM cart WHERE id = #{cartId}")
        Cart getById(Long cartId);

        @Delete("DELETE FROM cart WHERE user_id=#{userId}")
        void deleteAllCart(Long userId);
}
