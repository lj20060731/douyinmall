package com.douyinmall.order.domain.VO;

import com.douyinmall.common.entity.UserCouponVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_CONFIRMED = 2;
    public static final Integer CONFIRMED = 3;
    public static final Integer DELIVERY_IN_PROGRESS = 4;
    public static final Integer COMPLETED = 5;
    public static final Integer CANCELLED = 6;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 1;
    public static final Integer PAID = 2;
    public static final Integer REFUND = 3;

    /**
     * 配送状态  1送出  0未发货
     */
    public static final Integer SENT = 1;
    public static final Integer NOT_SENT = 0;

    private Long id;

    //订单号
    private String number;

    //订单状态 1待付款  2派送中 3已完成  4取消
    private Integer status;

    //下单用户id
    private Long userId;

    //地址id
    private Long addressId;

    //下单时间
    private LocalDateTime orderTime;

    //支付时间
    private LocalDateTime checkoutTime;

    //支付状态 1未支付 2已支付 3退款
    private Integer payStatus;

    //实收金额
    private Double totalAmount;

    //手机号
    private String phone;

    //地址
    private String address;

    //收货人
    private String consignee;

    //订单取消原因
    private String cancelReason;

    //订单取消时间
    private LocalDateTime cancelTime;

    //配送状态  1送出  0未发货 2到货
    private Integer deliveryStatus;

    //送达时间
    private LocalDateTime deliveryTime;

    //订单细节
    private List<OrderDetail> items;

    //使用优惠券
    private List<UserCouponVO> userCoupons;
}
