package com.douyinmall.promotion.controller;


import com.douyinmall.common.result.Result;
import com.douyinmall.promotion.domain.dto.CouponDiscountDTO;
import com.douyinmall.promotion.domain.dto.OrderProductDTO;
import com.douyinmall.promotion.domain.dto.UsedCouponDTO;
import com.douyinmall.promotion.domain.vo.UserCouponVO;
import com.douyinmall.promotion.service.IUserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-04-27
 */
@RestController
@RequestMapping("/promotion")
@RequiredArgsConstructor
public class UserCouponController {

        private final IUserCouponService userCouponService;

        @Operation(summary = "兑换码兑换优惠券接口")
        @PostMapping("/{code}/exchange")
        public Result<String> exchangeCoupon(@PathVariable("code") String code) {
                return userCouponService.exchangeCoupon(code);
        }

        @Operation(summary = "查询我的优惠券接口")
        @GetMapping("/mycoupon")
        public Result<List<UserCouponVO>> queryMyCoupon() {
                return userCouponService.queryMyCoupon();
        }

        @Operation(summary = "领取优惠券接口")
        @PostMapping("/{couponId}/receive")
        public Result<String> receiveCoupon(@PathVariable("couponId") Long couponId){
                return userCouponService.receiveCoupon(couponId);
        }

        @Operation(summary = "查询我的优惠券可用方案")
        @PostMapping("/available")
        public Result<List<CouponDiscountDTO>> findDiscountSolution(@RequestBody List<OrderProductDTO> orderCourses){
                List<CouponDiscountDTO> couponDiscountDTOS = userCouponService.queryAvailableCoupon(orderCourses);
                return Result.success(couponDiscountDTOS);
                //return Result.success(userCouponService.queryAvailableCoupon(orderCourses));
        }

        @Operation(summary = "核销优惠券")
        @PostMapping("/use")
        public void useCoupon(@RequestBody UsedCouponDTO usedCouponDTO){
                userCouponService.useCoupon(usedCouponDTO);
        }

        @PostMapping("/getUserCouponByOrderId")
        Result<Map<Long,List<UserCouponVO>>> getUserCouponByOrderId(@RequestBody List<Long> orderIds){
                return userCouponService.getUserCouponByOrderId(orderIds);
        }
}
