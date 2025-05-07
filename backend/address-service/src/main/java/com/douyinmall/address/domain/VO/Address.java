package com.douyinmall.address.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address implements Serializable {

        private Long id;

        //用户id
        private Long userId;

        //收货人
        private String consignee;

        //手机号
        private String phone;

        //性别 0 女 1 男
        private Integer sex;

        //省级名称
        private String provinceName;

        //市级名称
        private String cityName;

        //区级名称
        private String districtName;

        //详细地址
        private String detail;

        //是否默认 0否 1是
        private Integer isDefault;
}
