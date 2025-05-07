package com.douyinmall.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.douyinmall.promotion.domain.po.Coupon;
import com.douyinmall.promotion.domain.po.UserCoupon;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-03-29
 */
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

        @Select(
                "select c.id, c.discount_type, c.`specific`, c.threshold_amount, c.discount_value, c.max_discount_amount, uc.id as creater " +  // 末尾加空格
                        "from coupon c inner join user_coupon uc on c.id = uc.coupon_id " +  // 末尾加空格
                        "where uc.user_id = #{user} and uc.user_coupon_status = 1"
        )
        List<Coupon> queryMyCoupon(Long user);
}
