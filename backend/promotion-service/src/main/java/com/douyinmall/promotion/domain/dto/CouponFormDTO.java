package com.douyinmall.promotion.domain.dto;


import com.douyinmall.promotion.enums.DiscountType;
import com.douyinmall.promotion.enums.ObtainType;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@Schema(description = "优惠券表单数据")
@NoArgsConstructor
@AllArgsConstructor
public class CouponFormDTO {

        @Schema(description = "优惠券id，新增不需要添加，更新必填")
        private Long id;

        @Schema(description = "优惠券名称")
        @NotNull(value = "优惠券名称不能为空")
        @Size(max = 20, min = 4, message = "优惠券名称长度错误")
        private String name;

        @Schema(description = "是否添限定使用范围，true：限定了，false：没限定")
        private Boolean specific;

        @Schema(description = "优惠券使用范围")
        private List<Integer> scopes;

        @Schema(description = "优惠券类型，1：每满减，2：折扣，3：无门槛，4：普通满减")
        @NotNull(value = "优惠券折扣类型不能为空")

        private DiscountType discountType;

        @Schema(description = "折扣门槛，0代表无门槛")
        private Integer thresholdAmount;
        @Schema(description = "折扣值，满减填抵扣金额；打折填折扣值：80标示打8折")
        private Integer discountValue;
        @Schema(description = "最大优惠金额")
        private Integer maxDiscountAmount;

        @Schema(description = "优惠券总量")
        @Range(max = 5000, min = 1, message = "优惠券总量必须在1~5000")
        private Integer totalNum;
        @Schema(description = "每人领取的上限")
        @Range(max = 10, min = 1, message = "每人限领数量必须在1~10")
        private Integer userLimit;
        @Schema(description = "获取方式1：手动领取，2：指定发放（通过兑换码兑换）")
        @NotNull(value = "领取方式不能为空")

        private ObtainType obtainWay;
}