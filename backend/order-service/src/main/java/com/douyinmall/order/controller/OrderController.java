package com.douyinmall.order.controller;




import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.result.PageResult;
import com.douyinmall.common.result.Result;
import com.douyinmall.order.domain.DTO.OrderPageQuery;
import com.douyinmall.order.domain.DTO.OrderSubmit;
import com.douyinmall.order.domain.DTO.OrderUpdate;
import com.douyinmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
        @Autowired
        private OrderService orderService;
        @Autowired
        private TokenHolder tokenHolder;
        @RequestMapping("/submit")
        public Result<Map<String,String>> submit(@RequestBody OrderSubmit orderSubmit) {
                try {
                        orderService.submit(orderSubmit);
                        Map<String, String> responseMap = new HashMap<>();
                        responseMap.put("message", "操作成功");
                        return Result.success(responseMap);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        @RequestMapping("/getorderlist")
        public Result<PageResult> getOrder(@RequestBody OrderPageQuery query) {
                query.setUserId(tokenHolder.getUserId());
                PageResult pageResult = orderService.getOrder(query);
                return Result.success(pageResult);
        }

        @PostMapping("/cancel")
        public Result<Map<String,String>> cancel(@RequestBody OrderUpdate orderUpdate) {
                orderService.cancel(orderUpdate);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "操作成功");
                return Result.success(responseMap);
        }

        @PostMapping("/confirm")
        public Result<Map<String,String>> confirm(@RequestBody OrderUpdate orderUpdate) {
                orderService.confirm(orderUpdate);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "操作成功");
                return Result.success(responseMap);
        }

        @PostMapping("/drawback")
        public Result<Map<String,String>> drawback(@RequestBody OrderUpdate orderUpdate) {
                orderService.drawback(orderUpdate);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "操作成功");
                return Result.success(responseMap);
        }
}
