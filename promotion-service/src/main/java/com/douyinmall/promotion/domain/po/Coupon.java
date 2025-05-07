package com.douyinmall.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.douyinmall.promotion.enums.CouponStatus;
import com.douyinmall.promotion.enums.DiscountType;
import com.douyinmall.promotion.enums.ObtainType;
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
 * 优惠券的规则信息
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("coupon")
@Schema(description="优惠券的规则信息")
@NoArgsConstructor
@AllArgsConstructor
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "优惠券名称，可以和活动名称保持一致")
    private String name;

    @Schema(description = "优惠券类型，1：普通券。目前就一种，保留字段")
    private Integer type;

    @Schema(description = "折扣类型，1：每满减，2：折扣，3：无门槛，4：普通满减")
    private DiscountType discountType;

    @TableField("`specific`")
    @Schema(description = "是否限定作用范围，false：不限定，true：限定。默认false")
    private Boolean specific;

    @Schema(description = "获取方式：1：手动领取，2：兑换码")
    private ObtainType obtainWay;

    @Schema(description = "开始发放时间")
    private LocalDateTime issueBeginTime;

    @Schema(description = "结束发放时间")
    private LocalDateTime issueEndTime;

    @Schema(description = "优惠券有效期天数，0：表示有效期是指定有效期的")
    private Integer termDays;

    @Schema(description = "优惠券有效期开始时间")
    private LocalDateTime termBeginTime;

    @Schema(description = "优惠券有效期结束时间")
    private LocalDateTime termEndTime;

    @Schema(description = "优惠券配置状态，1：待发放，2：未开始   3：进行中，4：已结束，5：暂停")
    private CouponStatus status;

    @Schema(description = "总数量，不超过5000")
    private Integer totalNum;

    @Schema(description = "已发行数量，用于判断是否超发")
    private Integer issueNum;

    @Schema(description = "已使用数量")
    private Integer usedNum;

    @Schema(description = "每个人限领的数量，默认1")
    private Integer userLimit;

    @Schema(description = "拓展参数字段，保留字段")
    private String extParam;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private Long creater=0L;

    @Schema(description = "更新人")
    private Long updater=0L;


}
