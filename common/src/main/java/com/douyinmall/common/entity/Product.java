package com.douyinmall.common.entity;

import com.douyinmall.common.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {
        private Long id;
        private String image;
        private String name;
        private double price;
        private String description ;
        private Long stock;
        private ProductType productType;
}
