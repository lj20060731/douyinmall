package com.douyinmall.common.entity;


import com.douyinmall.common.enums.DiscountType;
import com.douyinmall.common.enums.ProductType;
import com.douyinmall.common.enums.UserCouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息
 * </p>
 *
 * @author author
 * @since 2025-03-29
 */
@Data
@Schema(description = "个人用户的优惠券信息")
public class UserCouponVO implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "优惠券id")
        private Long couponId;

        @Schema(description = "优惠券名称")
        private String name;

        @Schema(description = "是否限定使用范围")
        private Boolean specific;

        @Schema(description = "优惠券使用范围，1代表数码型，2代表食品型，3代表服装型")
        private List<ProductType> scopes;

        @Schema(description = "优惠券类型，1：每满减，2：折扣，3：无门槛，4：普通满减")
        private DiscountType discountType;

        @Schema(description = "折扣门槛，0代表无门槛")
        private Integer thresholdAmount;

        @Schema(description = "折扣值，满减填抵扣金额；打折填折扣值：80标示打8折")
        private Integer discountValue;

        @Schema(description = "最大优惠金额")
        private Integer maxDiscountAmount;

        @Schema(description = "优惠券有效期开始时间")
        private LocalDateTime termBeginTime;

        @Schema(description = "优惠券有效期结束时间")
        private LocalDateTime termEndTime;

        @Schema(description = "优惠券可使用时间（核销时间）")
        private LocalDateTime usedTime;

        @Schema(description = "优惠券状态，1：未使用，2：已使用，3：已失效")
        private UserCouponStatus userCouponStatus;


}
