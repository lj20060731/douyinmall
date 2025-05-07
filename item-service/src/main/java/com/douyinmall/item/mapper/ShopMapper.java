package com.douyinmall.item.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyinmall.item.domain.DTO.ProductPageQuery;
import com.douyinmall.item.domain.VO.Product;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShopMapper extends BaseMapper<Product> {
        Page<Product> pageQuery(ProductPageQuery query);

        @Select("SELECT * FROM product WHERE id = #{productId}")
        Product getProductById(Long productId);

        @Select("SELECT COUNT(*) FROM product")
        Integer getProductsNum();
}
