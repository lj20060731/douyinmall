package com.douyinmall.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * 促销活动，形式多种多样，例如：优惠券
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("promotion")
@Schema(description="促销活动，形式多种多样，例如：优惠券")
@NoArgsConstructor
@AllArgsConstructor
public class Promotion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "促销活动id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "促销活动类型：1-优惠券，2-分销")
    private Integer type;

    @Schema(description = "是否是热门活动：true或false，默认false")
    private Integer hot;

    @Schema(description = "活动开始时间")
    private LocalDateTime beginTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private Long creater=0L;

    @Schema(description = "更新人")
    private Long updater=0L;


}
