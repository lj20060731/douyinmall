package com.douyinmall.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.douyinmall.common.result.Result;
import com.douyinmall.promotion.domain.dto.CouponDiscountDTO;

import com.douyinmall.promotion.domain.dto.OrderProductDTO;
import com.douyinmall.promotion.domain.dto.UsedCouponDTO;
import com.douyinmall.promotion.domain.dto.UserCouponDTO;
import com.douyinmall.promotion.domain.po.UserCoupon;
import com.douyinmall.promotion.domain.vo.UserCouponVO;


import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务类
 * </p>
 *
 * @author author
 * @since 2025-03-29
 */
public interface IUserCouponService extends IService<UserCoupon> {

        Result<String> receiveCoupon(Long id);

        Result<String> exchangeCoupon(String code);


        List<CouponDiscountDTO> queryAvailableCoupon(List<OrderProductDTO> courses);

        Result<List<UserCouponVO>> queryMyCoupon();

        void checkAndCreateUserCouponNew(UserCouponDTO dto);

        void useCoupon(UsedCouponDTO usedCouponDTO);

        Result<Map<Long,List<UserCouponVO>>> getUserCouponByOrderId(List<Long> orderIds);
}
