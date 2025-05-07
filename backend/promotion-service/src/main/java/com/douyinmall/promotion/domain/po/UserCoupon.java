package com.douyinmall.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.douyinmall.promotion.enums.UserCouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息
 * </p>
 *
 * @author author
 * @since 2025-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_coupon")
@Schema(description="用户领取优惠券的记录，是真正使用的优惠券信息")
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户券id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "优惠券的拥有者")
    private Long userId;

    @Schema(description = "优惠券模板id")
    private Long couponId;

    @Schema(description = "优惠券有效期开始时间")
    private LocalDateTime termBeginTime;

    @Schema(description = "优惠券有效期结束时间")
    private LocalDateTime termEndTime;

    @Schema(description = "优惠券使用时间（核销时间）")
    private LocalDateTime usedTime;

    @Schema(description = "使用该优惠券的订单id")
    private Long orderId;

    @Schema(description = "优惠券状态，1：未使用，2：已使用，3：已失效")
    private UserCouponStatus userCouponStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
