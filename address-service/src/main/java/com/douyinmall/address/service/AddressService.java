package com.douyinmall.address.service;



import com.douyinmall.address.domain.VO.Address;
import com.douyinmall.common.result.Result;

import java.util.List;
import java.util.Map;

public interface AddressService {
        void addAddress(Address address);

        List<Address> getAddressList(Long userId);

        void setDefault(Address address);

        void update(Address address);

        void delete(Long id);

        Address getDefault(Long userId);


        Address getAddressById(Long addressId);
}
