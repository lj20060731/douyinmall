package com.douyinmall.promotion.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "订单中商品及优惠券信息")
public class OrderCouponDTO {
    @Schema(description ="用户优惠券id")
    private List<Long> userCouponIds;
    @Schema(description ="订单中的商品列表")
    private List<OrderProductDTO> productList;
}
