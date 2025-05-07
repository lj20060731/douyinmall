package com.douyinmall.cart.listener;

import com.douyinmall.cart.service.CartService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartListener {
        @Autowired
        private CartService cartService;

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = "cart.queue", durable = "true"),
                exchange = @Exchange(name = "cart.exchange"),
                key = "cart"
        ))
        public void listenDeleteCart(Long UserId) {
                cartService.deleteAllCart(UserId);
        }
}
