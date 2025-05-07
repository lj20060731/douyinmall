package com.douyinmall.order.listener;

import com.douyinmall.order.config.RabbitMQConfig;
import com.douyinmall.order.domain.DTO.OrderUpdate;
import com.douyinmall.order.domain.VO.Order;
import com.douyinmall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

        private final OrderService orderService;

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = RabbitMQConfig.ORDER_DELAY_QUEUE),
                exchange = @Exchange(name = RabbitMQConfig.ORDER_DELAY_EXCHANGE,delayed = "true"),
                key = RabbitMQConfig.ORDER_DELAY_ROUTING_KEY
        ))
        public void listenDelayMessage(Long orderId) {
                Order order = orderService.getOrderById(orderId);
                if (order == null || order.getPayStatus()==1) {
                        return;
                }
                //todo
        }

}
