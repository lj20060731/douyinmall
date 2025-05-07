package com.douyinmall.promotion.discount;

import com.douyinmall.common.utils.NumberUtils;
import com.douyinmall.common.utils.StringUtils;
import com.douyinmall.promotion.domain.po.Coupon;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PerPriceDiscount implements Discount {

    private final static String RULE_TEMPLATE = "每满{}减{}，上限{}";

    @Override
    public boolean canUse(int totalAmount, Coupon coupon) {
        return totalAmount >= coupon.getThresholdAmount();
    }

    @Override
    public int calculateDiscount(int totalAmount, Coupon coupon) {
        int discount = 0;
        Integer thresholdAmount = coupon.getThresholdAmount();
        Integer discountValue = coupon.getDiscountValue();
        while (totalAmount >= thresholdAmount) {
            discount += discountValue;
            totalAmount -= thresholdAmount;
        }
        return Math.min(discount, coupon.getMaxDiscountAmount());
    }

    @Override
    public String getRule(Coupon coupon) {
        return StringUtils.format(
                RULE_TEMPLATE,
                coupon.getThresholdAmount(),
                coupon.getDiscountValue(),
                coupon.getMaxDiscountAmount());
    }
}
