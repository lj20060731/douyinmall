package com.douyinmall.item.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

        public static final String STOCK_EXCHANGE = "stock.exchange";

        @Bean
        public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
                RabbitTemplate template = new RabbitTemplate(connectionFactory);
                template.setMessageConverter(jackson2JsonMessageConverter(objectMapper));
                return template;
        }

        @Bean
        public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
                return new Jackson2JsonMessageConverter(objectMapper);
        }

        @Bean
        public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
                ConnectionFactory connectionFactory,
                Jackson2JsonMessageConverter messageConverter
        ) {
                SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
                factory.setConnectionFactory(connectionFactory);
                factory.setMessageConverter(messageConverter);
                return factory;
        }


        @Bean
        public DirectExchange stockExchange() {
                return new DirectExchange(STOCK_EXCHANGE);
        }

        @Bean
        public Queue stockReduce() {
                return new Queue("reduceQueue", true);
        }

        @Bean
        Queue stockAdd() {
                return new Queue("addQueue", true);
        }


        @Bean
        public Binding reduce(Queue stockReduce, DirectExchange stockExchange) {
                return BindingBuilder.bind(stockReduce).to(stockExchange).with("reduce");
        }

        @Bean
        public Binding add(Queue stockAdd, DirectExchange stockExchange) {
                return BindingBuilder.bind(stockAdd).to(stockExchange).with("add");
        }

}