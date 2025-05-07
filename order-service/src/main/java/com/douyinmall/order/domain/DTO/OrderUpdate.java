package com.douyinmall.order.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdate {

        private Long id;

        //订单状态 1待付款  2派送中 3已完成  4取消
        private Integer status;

        //支付状态 0未支付 1已支付 2退款
        private Integer payStatus;

        //配送状态  1送出  0未发货 2到货
        private Integer deliveryStatus;

        //订单取消原因
        private String cancelReason;

        //订单取消时间
        private LocalDateTime cancelTime;

        //订单支付时间
        private LocalDateTime checkoutTime;

        //送达时间
        private LocalDateTime deliveryTime;
}
