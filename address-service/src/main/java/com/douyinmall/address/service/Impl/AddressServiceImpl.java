package com.douyinmall.address.service.Impl;


import com.douyinmall.address.domain.VO.Address;
import com.douyinmall.address.mapper.AddressMapper;
import com.douyinmall.address.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
        @Autowired
        private AddressMapper addressMapper;

        @Override
        public void addAddress(Address address) {
                addressMapper.addAddress(address);
        }

        @Override
        public List<Address> getAddressList(Long userId) {
                return addressMapper.getAddressList(userId);
        }

        @Override
        public void setDefault(Address address) {
                addressMapper.update(address);
        }

        @Override
        public void update(Address address) {
                addressMapper.update(address);
        }

        @Override
        public void delete(Long id) {
                addressMapper.delete(id);
        }

        @Override
        public Address getDefault(Long userId) {
                return addressMapper.getDefault(userId);
        }

        @Override
        public Address getAddressById(Long addressId) {
                return addressMapper.getById(addressId);
        }


}
