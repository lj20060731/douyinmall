package com.douyinmall.order.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail implements Serializable {

    private Long id;

    //名称
    private String name;

    //图片
    private String image;

    //订单id
    private Long orderId;

    //商品id
    private Long productId;

    //数量
    private Integer number;

    //金额
    private Double amount;
}
