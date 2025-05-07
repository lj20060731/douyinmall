package com.douyinmall.promotion.controller;


import com.douyinmall.common.result.Result;
import com.douyinmall.promotion.domain.dto.CouponFormDTO;
import com.douyinmall.promotion.domain.dto.CouponIssueFormDTO;
import com.douyinmall.promotion.domain.dto.PageDTO;
import com.douyinmall.promotion.domain.query.CouponQuery;
import com.douyinmall.promotion.domain.vo.CouponPageVO;
import com.douyinmall.promotion.domain.vo.CouponVO;
import com.douyinmall.promotion.service.ICouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 优惠券的规则信息 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
@Tag(name = "优惠券相关接口", description = "提供优惠券的新增、查询、发放等操作接口")
@RestController
@RequestMapping("/promotion")
@RequiredArgsConstructor
public class CouponController {

        private final ICouponService couponService;

        @Operation(summary = "新增优惠券", description = "根据传入的优惠券表单信息新增一张优惠券")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "优惠券新增成功")
        })
        @PostMapping()
        public void saveCoupon(@RequestBody @Validated CouponFormDTO dto) {
                couponService.saveCoupon(dto);
        }

        @Operation(summary = "分页查询优惠券列表--管理端", description = "根据查询条件分页查询优惠券列表，用于管理端展示")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "成功返回优惠券分页列表")
        })
        @GetMapping()
        public Result<PageDTO<CouponPageVO>> queryCouponPage(@Parameter(description = "优惠券查询条件对象") CouponQuery query) {
                return Result.success(couponService.queryCouponPage(query));
        }

        @Operation(summary = "发放优惠券", description = "根据传入的发放表单信息和优惠券 ID 发放优惠券")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "优惠券发放成功")
        })
        @PutMapping("{id}/issue")
        public void issueCoupon(@RequestBody CouponIssueFormDTO dto, @Parameter(description = "优惠券 ID") @PathVariable Long id) {
                couponService.issueCoupon(dto, id);
        }

        @Operation(summary = "查询优惠券", description = "查询所有优惠券信息列表")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "成功返回优惠券列表")
        })
        @GetMapping("/list")
        public Result<List<CouponVO>> queryCouponList() {
                return Result.success(couponService.queryCouponList());
        }

        @Operation(summary = "暂停发放优惠券", description = "根据优惠券 ID 暂停发放优惠券")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "优惠券暂停发放成功")
        })
        @PutMapping("{id}/pause")
        public void pauseIssue(@Parameter(description = "优惠券 ID") @PathVariable Long id) {
                couponService.pauseIssue(id);
        }

}