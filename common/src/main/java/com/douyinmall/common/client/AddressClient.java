package com.douyinmall.common.client;

import com.douyinmall.common.entity.Address;
import com.douyinmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("address-service")
public interface AddressClient {
        @GetMapping("/address/getaddressbyid")
        Result<Address> getAddressById(@RequestParam("addressId") Long addressId);
}
