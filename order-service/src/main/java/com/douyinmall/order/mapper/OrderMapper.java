package com.douyinmall.order.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyinmall.order.domain.DTO.OrderPageQuery;
import com.douyinmall.order.domain.DTO.OrderUpdate;
import com.douyinmall.order.domain.VO.Order;
import com.douyinmall.order.domain.VO.OrderDetail;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {
        int insert(Order order);

        void insertOrderDetail(OrderDetail orderDetail);

        Page<Order> pageQuery(OrderPageQuery query);

        List<OrderDetail> selectDetailsByOrderIds(List<Long> orderIds);

        void updateStatus(OrderUpdate orderUpdate);

        @Select("select * from `order` where id = #{orderId}")
        Order getOrderById(Long orderId);

        @Select("select * from order_detail where order_id= #{id}")
        List<OrderDetail> getOrderDetails(Long id);

}
