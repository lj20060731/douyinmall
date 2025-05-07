package com.douyinmall.item.domain.VO;

import com.baomidou.mybatisplus.annotation.TableName;
import com.douyinmall.common.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("product")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Product implements Serializable {
        private Long id;
        private String image;
        private String name;
        private double price;
        private String description ;
        private Integer stock;
        private ProductType productType;
}
