package com.douyinmall.common.client;

import ch.qos.logback.core.spi.ConfigurationEvent;
import com.douyinmall.common.entity.UsedCouponDTO;
import com.douyinmall.common.entity.UserCouponVO;
import com.douyinmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;


@FeignClient(name = "promotion-service" ,fallbackFactory = PromotionClientFallbackFactory.class)
public interface PromotionClient {
        @PostMapping("/promotion/use")
        void useCoupon(@RequestBody UsedCouponDTO usedCouponDTO);
        @PostMapping("/promotion/getUserCouponByOrderId")
        Result<Map<Long,List<UserCouponVO>>> getUserCouponByOrderId(List<Long> orderIds);
}
