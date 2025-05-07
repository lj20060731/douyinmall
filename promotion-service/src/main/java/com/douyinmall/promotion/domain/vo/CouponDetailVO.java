package com.douyinmall.promotion.domain.vo;


import com.douyinmall.common.utils.DateUtils;
import com.douyinmall.promotion.enums.DiscountType;
import com.douyinmall.promotion.enums.ObtainType;
import com.douyinmall.promotion.enums.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "优惠券详细数据")
public class CouponDetailVO {
    @Schema(description ="优惠券id，新增不需要添加，更新必填")
    private Long id;

    @Schema(description ="优惠券名称")
    private String name;

    @Schema(description ="优惠券使用范围，1代表数码型，2代表食品型，3代表服装型")
    private List<ProductType> scopes;

    @Schema(description ="优惠券类型，1：每满减，2：折扣，3：无门槛，4：普通满减")
    private DiscountType discountType;
    @Schema(description ="折扣门槛，0代表无门槛")
    private Integer thresholdAmount;
    @Schema(description ="折扣值，满减填抵扣金额；打折填折扣值：80标示打8折")
    private Integer discountValue;
    @Schema(description ="最大优惠金额")
    private Integer maxDiscountAmount;

    @Schema(description ="发放开始时间")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime issueBeginTime;
    @Schema(description ="发放结束时间")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime issueEndTime;

    @Schema(description ="有效天数")
    private Integer termDays;
    @Schema(description ="使用有效期开始时间")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime termBeginTime;
    @Schema(description ="使用有效期结束时间")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime termEndTime;

    @Schema(description ="优惠券总量，如果为0代表无上限")
    private Integer totalNum;
    @Schema(description ="每人领取的上限")
    private Integer userLimit;
    @Schema(description ="获取方式1：手动领取，2：指定发放（通过兑换码兑换）")
    private ObtainType obtainWay;
}
