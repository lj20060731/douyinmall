package com.douyinmall.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.douyinmall.promotion.domain.po.Coupon;
import com.douyinmall.promotion.domain.po.ExchangeCode;


/**
 * <p>
 * 兑换码 服务类
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
public interface IExchangeCodeService extends IService<ExchangeCode> {

        void asyncGenerateExchangeCode(Coupon coupon);

        boolean updateExchangeCodeMark(long serialNum, boolean b);
}
