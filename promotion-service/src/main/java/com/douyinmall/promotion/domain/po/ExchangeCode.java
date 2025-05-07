package com.douyinmall.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.douyinmall.promotion.enums.ExchangeCodeStatus;
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
 * 兑换码
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exchange_code")
@Schema(description="兑换码")
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "兑换码id")
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @Schema(description = "兑换码")
    private String code;

    @Schema(description = "兑换码状态， 1：待兑换，2：已兑换，3：兑换活动已结束")
    private ExchangeCodeStatus status;

    @Schema(description = "兑换人")
    private Long userId;

    @Schema(description = "兑换类型，1：优惠券，以后再添加其它类型")
    private Integer type;

    @Schema(description = "兑换码目标id，例如兑换优惠券，该id则是优惠券的配置id")
    private Long exchangeTargetId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "兑换码过期时间")
    private LocalDateTime expiredTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}
