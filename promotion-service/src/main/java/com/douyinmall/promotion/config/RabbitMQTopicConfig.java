package com.douyinmall.promotion.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQTopicConfig {
        @Bean
        public TopicExchange promotionTopicExchange() {
                return new TopicExchange(
                        "promotion.topic"
                );
        }


        @Bean
        public Queue couponReceiveQueue() {
                return new Queue("coupon.receive.queue", true);
        }


        @Bean
        public Binding couponReceiveBinding(Queue couponReceiveQueue, TopicExchange promotionTopicExchange) {
                return BindingBuilder.bind(couponReceiveQueue)
                        .to(promotionTopicExchange)
                        .with("coupon.receive");
        }

}