package com.douyinmall.order.domain.DTO;

import com.douyinmall.common.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSubmit {
    private Long addressId;

    private List<Long> cartId;

    private Double totalAmount;

    private Integer payStatus;

    private List<Long> couponIds;
}
