package com.douyinmall.promotion.domain.query;


import com.douyinmall.common.result.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;


@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "兑换码查询参数")
public class CodeQuery extends PageQuery {
    @Schema(description = "兑换码对应的优惠券id")
    @NotNull
    private Long couponId;
    @Schema(description = "兑换码状态，1：未兑换，2：已兑换")
    @NotNull()
    private Integer status;
}