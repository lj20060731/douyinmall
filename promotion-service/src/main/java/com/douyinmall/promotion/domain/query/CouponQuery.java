package com.douyinmall.promotion.domain.query;


import com.douyinmall.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "优惠券查询参数")
@Accessors(chain = true)
public class CouponQuery extends PageQuery {

    @Schema(description = "优惠券折扣类型：1：每满减，2：折扣，3：无门槛，4：满减")
    private Integer type;

    @Schema(description = "优惠券状态，1：待发放，2：发放中，3：已结束, 4：取消/终止")
    private Integer status;

    @Schema(description = "优惠券名称")
    private String name;
}