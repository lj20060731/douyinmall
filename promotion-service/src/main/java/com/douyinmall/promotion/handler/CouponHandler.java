package com.douyinmall.promotion.handler;


import com.douyinmall.promotion.domain.dto.UserCouponDTO;
import com.douyinmall.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponHandler {

        private final IUserCouponService couponService;

        @RabbitListener(
                bindings = @QueueBinding(
                        value = @Queue(value = "coupon.receive.queue",durable = "true"),
                        exchange = @Exchange(value = "promotion.topic",type = ExchangeTypes.TOPIC),
                        key = "coupon.receive"
                )
        )
        public void onMsg(UserCouponDTO dto){
                couponService.checkAndCreateUserCouponNew(dto);
        }
}
