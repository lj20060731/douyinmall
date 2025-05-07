package com.douyinmall.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsedCouponDTO {
        private Long orderId;
        private List<Long> couponIds;
}
