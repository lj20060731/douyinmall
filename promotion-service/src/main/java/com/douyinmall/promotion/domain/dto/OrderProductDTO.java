package com.douyinmall.promotion.domain.dto;


import com.douyinmall.promotion.enums.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单中的商品信息")
public class OrderProductDTO {
    @Schema(description ="商品id")
    private Long id;
    @Schema(description ="商品的分类id")
    private ProductType productType;
    @Schema(description ="商品价格")
    private Integer price;
}
