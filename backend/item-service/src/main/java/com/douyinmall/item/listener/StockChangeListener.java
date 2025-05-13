package com.douyinmall.item.listener;


import com.douyinmall.common.entity.StockChangeMessage;
import com.douyinmall.item.config.RabbitMQConfig;
import com.douyinmall.item.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class StockChangeListener {

        private final ShopService shopService;

        @RabbitListener(
                bindings = @QueueBinding(
                        value = @Queue(name = "reduceQueue"),
                        exchange = @Exchange(name = RabbitMQConfig.STOCK_EXCHANGE),
                        key = "reduce"
                ),
                containerFactory = "rabbitListenerContainerFactory"
        )
        public void listenReduceMessage(List<StockChangeMessage> messages) {
                Map<Long, Integer> productIdToNumberMap = new HashMap<>();
                messages.forEach(msg ->
                        productIdToNumberMap.put(msg.getProductId(), msg.getQuantity())
                );
                shopService.reduce(productIdToNumberMap);
        }

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = "addQueue"),
                exchange = @Exchange(name = RabbitMQConfig.STOCK_EXCHANGE),
                key = "add"
        ))
        public void listenAddMessage(List<StockChangeMessage> messages) {
                Map<Long, Integer> productIdToNumberMap = new HashMap<>();
                messages.forEach(msg ->
                        productIdToNumberMap.put(msg.getProductId(), msg.getQuantity())
                );
                shopService.add(productIdToNumberMap);
        }



}
