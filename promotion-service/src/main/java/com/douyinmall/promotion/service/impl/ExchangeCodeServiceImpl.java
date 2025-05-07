package com.douyinmall.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.douyinmall.promotion.domain.po.Coupon;
import com.douyinmall.promotion.domain.po.ExchangeCode;
import com.douyinmall.promotion.mapper.ExchangeCodeMapper;
import com.douyinmall.promotion.service.IExchangeCodeService;
import com.douyinmall.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.douyinmall.promotion.constants.PromotionConstants.COUPON_CODE_MAP_KEY;
import static com.douyinmall.promotion.constants.PromotionConstants.COUPON_CODE_SERIAL_KEY;


/**
 * <p>
 * 兑换码 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
@Service
@RequiredArgsConstructor
@EnableAsync
public class ExchangeCodeServiceImpl extends ServiceImpl<ExchangeCodeMapper, ExchangeCode> implements IExchangeCodeService {

        private final StringRedisTemplate redisTemplate;
        @Override
        @Async
        public void asyncGenerateExchangeCode(Coupon coupon) {
                Integer totalNum = coupon.getTotalNum();
                Long increment = redisTemplate.opsForValue().increment(COUPON_CODE_SERIAL_KEY, totalNum);
                if (increment == null) {
                        return;
                }
                int maxSerial = increment.intValue();
                int begin=maxSerial-totalNum+1;
                List<ExchangeCode> exchangeCodes = new ArrayList<>();
                for (int i = begin; i <= maxSerial; i++) {
                        String s = CodeUtil.generateCode(i, coupon.getId());
                        ExchangeCode exchangeCode = new ExchangeCode();
                        exchangeCode.setId(i);
                        exchangeCode.setCode(s);
                        exchangeCode.setExchangeTargetId(coupon.getId());
                        exchangeCode.setExpiredTime(coupon.getIssueEndTime());
                        exchangeCodes.add(exchangeCode);
                }
                saveBatch(exchangeCodes);
        }

        @Override
        public boolean updateExchangeCodeMark(long serialNum, boolean flag) {
                Boolean setBit = redisTemplate.opsForValue().setBit(COUPON_CODE_MAP_KEY, serialNum, flag);
                return setBit != null && setBit;
        }
}
