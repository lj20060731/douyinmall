package com.douyinmall.promotion.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.douyinmall.promotion.domain.dto.CouponFormDTO;
import com.douyinmall.promotion.domain.dto.CouponIssueFormDTO;
import com.douyinmall.promotion.domain.dto.PageDTO;
import com.douyinmall.promotion.domain.po.Coupon;
import com.douyinmall.promotion.domain.query.CouponQuery;
import com.douyinmall.promotion.domain.vo.CouponPageVO;
import com.douyinmall.promotion.domain.vo.CouponVO;


import java.util.List;

/**
 * <p>
 * 优惠券的规则信息 服务类
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
public interface ICouponService extends IService<Coupon> {

        void saveCoupon(CouponFormDTO dto);

        PageDTO<CouponPageVO> queryCouponPage(CouponQuery query);

        void issueCoupon(CouponIssueFormDTO dto, Long id);

        List<CouponVO> queryCouponList();

        public void pauseIssue(Long id);

}
