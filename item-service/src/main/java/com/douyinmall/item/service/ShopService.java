package com.douyinmall.item.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.douyinmall.common.result.PageResult;
import com.douyinmall.item.domain.DTO.ProductPageQuery;
import com.douyinmall.item.domain.VO.Product;

import java.util.Map;


public interface ShopService extends IService<Product> {

        PageResult getProductList(ProductPageQuery query);

        Product getProductById(Long productId);

        Integer getProductsNum();

        void reduce(Map<Long, Integer> map);

        void add(Map<Long, Integer> map);
}
