package com.douyinmall.order.service.Impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.client.AddressClient;
import com.douyinmall.common.client.CartClient;
import com.douyinmall.common.client.PromotionClient;
import com.douyinmall.common.entity.*;
import com.douyinmall.common.result.PageResult;
import com.douyinmall.common.result.Result;
import com.douyinmall.order.config.RabbitMQConfig;
import com.douyinmall.order.domain.DTO.OrderPageQuery;
import com.douyinmall.order.domain.DTO.OrderSubmit;
import com.douyinmall.order.domain.DTO.OrderUpdate;
import com.douyinmall.order.domain.VO.Order;
import com.douyinmall.order.domain.VO.OrderDetail;
import com.douyinmall.order.mapper.OrderMapper;
import com.douyinmall.order.service.OrderService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.douyinmall.order.config.RabbitMQConfig.CART_EXCHANGE;
import static com.douyinmall.order.config.RabbitMQConfig.STOCK_EXCHANGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

        private final OrderMapper orderMapper;
        private final TokenHolder tokenHolder;
        private final AddressClient addressClient;
        private final CartClient cartClient;
        private final PromotionClient promotionClient;
        private final RabbitTemplate rabbitTemplate;
        private final StringRedisTemplate stringRedisTemplate;
        private final ObjectMapper objectMapper;

        @GlobalTransactional
        @Override
        public boolean submit(OrderSubmit orderSubmit) {
                Result<Address> addressById = addressClient.getAddressById(orderSubmit.getAddressId());
                Address address = addressById.getData();
                Order order = new Order();
                Long userId = tokenHolder.getUserId();
                order.setNumber(String.valueOf(System.currentTimeMillis()));
                order.setStatus(Order.PENDING_PAYMENT);
                order.setUserId(userId);
                order.setAddressId(orderSubmit.getAddressId());
                order.setOrderTime(LocalDateTime.now());
                order.setTotalAmount(orderSubmit.getTotalAmount());
                order.setPhone(address.getPhone());
                order.setAddress(address.getProvinceName() + address.getCityName() + address.getDistrictName() + address.getDetail());
                order.setConsignee(address.getConsignee());
                if (orderSubmit.getPayStatus() == 1) {
                        order.setPayStatus(Order.UN_PAID);
                        order.setCheckoutTime(null);
                        order.setStatus(1);
                } else {
                        order.setPayStatus(Order.PAID);
                        order.setCheckoutTime(LocalDateTime.now());
                        order.setStatus(2);
                        Result<List<Cart>> cartResponse = cartClient.getCartByIds(orderSubmit.getCartId());
                        if (cartResponse == null || cartResponse.getCode() != 200) {
                                log.error("获取购物车数据失败");
                                return false;
                        }
                        List<Cart> carts = cartResponse.getData();
                        if (carts.isEmpty()) {
                                return false;
                        }
                        Map<Long, Integer> productIdToNumberMap = carts.stream()
                                .collect(Collectors.toMap(
                                        Cart::getProductId,
                                        Cart::getNumber,
                                        Integer::sum,  // 合并重复商品的数量
                                        HashMap::new
                                ));
                        boolean b = reduceStockWithLua(productIdToNumberMap);
                        if (!b) {
                                return false;
                        }
                }
                orderMapper.insert(order);
                if (orderSubmit.getCouponIds() != null && !orderSubmit.getCouponIds().isEmpty()) {
                        //核销优惠券，远程调用
                        UsedCouponDTO usedCouponDTO = new UsedCouponDTO();
                        usedCouponDTO.setCouponIds(orderSubmit.getCouponIds());
                        usedCouponDTO.setOrderId(order.getId());
                        promotionClient.useCoupon(usedCouponDTO);
                }
                for (Long cartId : orderSubmit.getCartId()) {
                        Cart cart = cartClient.getCartById(cartId).getData();
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setName(cart.getName());
                        orderDetail.setImage(cart.getImage());
                        orderDetail.setOrderId(order.getId());
                        orderDetail.setProductId(cart.getProductId());
                        orderDetail.setNumber(cart.getNumber());
                        orderDetail.setAmount(cart.getAmount());
                        orderMapper.insertOrderDetail(orderDetail);
                }
                //删除购物车中的数据
                rabbitTemplate.convertAndSend(CART_EXCHANGE, "cart", userId);
                //延迟消息，判断是否支付
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_DELAY_EXCHANGE,
                        RabbitMQConfig.ORDER_DELAY_ROUTING_KEY,
                        order.getId(),
                        new MessagePostProcessor() {
                                @Override
                                public Message postProcessMessage(Message message) {
                                        message.getMessageProperties().getHeaders().put("x-delay", 10000); // 延迟 10 秒
                                        return message;
                                }
                        });
                return true;
        }

        @Override
        public PageResult getOrder(OrderPageQuery query) {
                PageHelper.startPage(query.getPage(), query.getPageSize());
                // 1.先分页查询主表
                Page<Order> pageQuery = orderMapper.pageQuery(query);
                // 2.批量查询明细
                if (!pageQuery.isEmpty()) {
                        List<Long> orderIds = pageQuery.stream().map(Order::getId).collect(Collectors.toList());
                        Map<Long,List<UserCouponVO>> map = promotionClient.getUserCouponByOrderId(orderIds).getData();
                        List<OrderDetail> details = orderMapper.selectDetailsByOrderIds(orderIds);
                        // 3.手动组装数据
                        Map<Long, List<OrderDetail>> detailMap = details.stream()
                                .collect(Collectors.groupingBy(OrderDetail::getOrderId));
                        pageQuery.forEach(order ->
                                {
                                        order.setItems(detailMap.getOrDefault(order.getId(), Collections.emptyList()));
                                        order.setUserCoupons(map.getOrDefault(order.getId(), Collections.emptyList()));
                                }
                        );
                }
                return new PageResult(pageQuery.getTotal(), pageQuery.getResult());
        }

        @Override
        public void cancel(OrderUpdate orderUpdate) {
                orderUpdate.setCancelTime(LocalDateTime.now());
                orderMapper.updateStatus(orderUpdate);
        }

        @Override
        public boolean confirm(OrderUpdate orderUpdate) {
                orderUpdate.setCheckoutTime(LocalDateTime.now());
                orderMapper.updateStatus(orderUpdate);
                List<OrderDetail> orderDetails = orderMapper.getOrderDetails(orderUpdate.getId());
                Map<Long, Integer> collect = orderDetails.stream().collect(Collectors.toMap(OrderDetail::getProductId, OrderDetail::getNumber));
                return reduceStockWithLua(collect);
        }

        @Override
        public void drawback(OrderUpdate orderUpdate) {
                orderUpdate.setCancelTime(LocalDateTime.now());
                orderMapper.updateStatus(orderUpdate);
                List<OrderDetail> orderDetails = orderMapper.getOrderDetails(orderUpdate.getId());
                Map<Long, Integer> collect = orderDetails.stream().collect(Collectors.toMap(OrderDetail::getProductId, OrderDetail::getNumber));
                rabbitTemplate.convertAndSend(STOCK_EXCHANGE, "add", collect);
        }

        @Override
        public Order getOrderById(Long orderId) {
                return orderMapper.getOrderById(orderId);
        }

        @Override
        public List<OrderDetail> getOrderDetails(Long id) {
                return orderMapper.getOrderDetails(id);
        }

        private boolean reduceStockWithLua(Map<Long, Integer> productIdToNumberMap) {
                //收集 KEYS（商品缓存键）和 ARGV（扣减数量）
                List<String> keys = new ArrayList<>();  // 对应 KEYS[1...n]
                List<String> argsList = new ArrayList<>();  // 对应 ARGV[1...n]
                for (Map.Entry<Long, Integer> entry : productIdToNumberMap.entrySet()) {
                        String key = "shop:" + entry.getKey();
                        stringRedisTemplate.expire(key, 1, TimeUnit.MINUTES);
                        keys.add(key);  // 构造商品缓存键（如 "shop:1"）
                        argsList.add(entry.getValue().toString()); // 扣减数量（转为字符串）
                }
                // 加载并执行 Lua 脚本
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("batch_stock_reduce.lua")));
                script.setResultType(Long.class); // 脚本返回值类型为 Long
                // 将 argsList 转换为 String[] 数组
                Long result = stringRedisTemplate.execute(
                        script,
                        keys,
                        argsList.toArray()  // List<String> → String[]
                );
                // 处理脚本返回结果
                if (result == 1) {
                        log.info("所有商品库存扣减成功");
                        // 发送消息，更新数据库并删除缓存
                        // rabbitTemplate.convertAndSend(STOCK_EXCHANGE, "reduce", productIdToNumberMap);
                        // 原始Map结构转换为DTO列表
                        List<StockChangeMessage> messages = productIdToNumberMap.entrySet().stream()
                                .map(entry -> new StockChangeMessage(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList());
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.STOCK_EXCHANGE,
                                "reduce",
                                messages,
                                message -> {
                                        message.getMessageProperties().setHeader("__TypeId__", StockChangeMessage.class.getName());
                                        return message;
                                }
                        );
                        for (Map.Entry<Long, Integer> entry : productIdToNumberMap.entrySet()) {
                                String key = "shop:" + entry.getKey();
                                String json = stringRedisTemplate.opsForValue().get(key);
                                if (json != null) {
                                        try {
                                                RedisData redisData = objectMapper.readValue(json, RedisData.class);
                                                redisData.setExpireTime(LocalDateTime.now());
                                                // 关键：指定序列化时保持数字类型（如Jackson配置）
                                                objectMapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
                                                String updatedJson = objectMapper.writeValueAsString(redisData);
                                                stringRedisTemplate.opsForValue().set(key, updatedJson);
                                        } catch (JsonProcessingException e) {
                                                log.error("缓存数据反序列化失败 key: {}", key, e);
                                        }
                                }
                        }
                        stringRedisTemplate.delete(stringRedisTemplate.keys("product_list:*"));
                        return true;
                } else if (result == -1) {
                        log.error("库存不足");
                        return false;
                } else if (result == -2) {
                        log.error("商品不存在");
                        return false;
                } else if (result == -3) {  // 新增处理分支
                        log.error("参数类型错误，库存或扣减值非数字");
                        return false;
                } else {
                        log.error("未知错误码: {}", result);
                        return false;
                }
        }
}