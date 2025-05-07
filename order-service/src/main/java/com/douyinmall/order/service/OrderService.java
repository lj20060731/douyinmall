package com.douyinmall.order.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.douyinmall.common.result.PageResult;
import com.douyinmall.order.domain.DTO.OrderPageQuery;
import com.douyinmall.order.domain.DTO.OrderSubmit;
import com.douyinmall.order.domain.DTO.OrderUpdate;
import com.douyinmall.order.domain.VO.Order;
import com.douyinmall.order.domain.VO.OrderDetail;

import java.util.List;


public interface OrderService extends IService<Order> {
        boolean submit(OrderSubmit orderSubmit);

        PageResult getOrder(OrderPageQuery query);

        void cancel(OrderUpdate orderUpdate);

        boolean confirm(OrderUpdate orderUpdate);

        void drawback(OrderUpdate orderUpdate);

        Order getOrderById(Long orderId);

        List<OrderDetail> getOrderDetails(Long id);

//        Object getOrderDetail(Long orderId);
}
