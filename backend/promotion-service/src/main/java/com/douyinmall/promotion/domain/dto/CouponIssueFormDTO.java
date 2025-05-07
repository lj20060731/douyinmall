package com.douyinmall.promotion.domain.dto;

import com.douyinmall.common.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "优惠券发放的表单实体")
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueFormDTO {
        @Schema(description = "优惠券id")
        private Long id;

        @Schema(description = "发放开始时间")
        @JsonFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
        @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
        @Future(message = "发放开始时间必须晚于当前时间")
        private LocalDateTime issueBeginTime;

        @Schema(description = "发放结束时间")
        @Future(message = "发放结束时间必须晚于当前时间")
        @NotNull(value = "发放结束时间不能为空")
        @JsonFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
        @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
        private LocalDateTime issueEndTime;

        @Schema(description = "有效天数")
        private Integer termDays;

        @Schema(description = "使用有效期开始时间")
        @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
        @JsonFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
        private LocalDateTime termBeginTime;

        @Schema(description = "使用有效期结束时间")
        @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
        @JsonFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
        private LocalDateTime termEndTime;
}
