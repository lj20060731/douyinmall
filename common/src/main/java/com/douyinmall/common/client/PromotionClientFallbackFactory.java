package com.douyinmall.common.client;

import com.douyinmall.common.entity.UsedCouponDTO;
import com.douyinmall.common.entity.UserCouponVO;
import com.douyinmall.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PromotionClientFallbackFactory implements FallbackFactory<PromotionClient> {
    
    @Override
    public PromotionClient create(Throwable cause) {
        return new PromotionClient() {
            @Override
            public void useCoupon(UsedCouponDTO dto) {
                log.warn("优惠券服务降级处理，原因: {}", cause.getMessage());
                // 可记录到本地数据库进行补偿
            }

            @Override
            public Result<Map<Long, List<UserCouponVO>>> getUserCouponByOrderId(List<Long> orderIds) {
                return null;
            }
        };
    }
}