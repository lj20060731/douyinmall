package com.douyinmall.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart implements Serializable {
        // 主键
        private Long id;
        // 商品名称
        private String name;
        // 图片
        private String image;
        // 用户id
        private Long userId;
        // 产品id
        private Long productId;
        // 数量
        private Integer number;
        // 金额
        private Double amount;
        // 创建时间
        private LocalDateTime createTime;
        //商品单价
        private Double price;
}
