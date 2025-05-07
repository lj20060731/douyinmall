package com.douyinmall.address.controller;


import com.douyinmall.address.domain.DTO.AddressAdd;
import com.douyinmall.address.domain.VO.Address;
import com.douyinmall.address.service.AddressService;
import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.entity.Cart;
import com.douyinmall.common.result.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/address")
public class AddressController {
        @Autowired
        private AddressService addressService;
        @Autowired
        private TokenHolder tokenHolder;

        @PostMapping("/add")
        public Result<String> add(@RequestBody AddressAdd addressAdd){
                Address address = new Address();
                BeanUtils.copyProperties(addressAdd,address);
                address.setUserId(tokenHolder.getUserId());
                address.setIsDefault(0);
                addressService.addAddress(address);
                return Result.success("添加成功");
        }
        @GetMapping("/get")
        public Result<List<Address>> get(){
                Long userId = tokenHolder.getUserId();
                List<Address> list = addressService.getAddressList(userId);
                return Result.success(list);
        }
        @PostMapping("/adddefault")
        public Result<Map<String, String>> setDefault(@RequestParam Long id){
                Address address = new Address();
                address.setId(id);
                address.setIsDefault(1);
                addressService.setDefault(address);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "操作成功");
                return Result.success(responseMap);
        }
        @PostMapping("/subdefault")
        public Result<Map<String, String>> subDefault(@RequestParam Long id){
                Address address = new Address();
                address.setId(id);
                address.setIsDefault(0);
                addressService.setDefault(address);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "操作成功");
                return Result.success(responseMap);
        }
        @PostMapping("/update")
        public Result<Map<String, String>> update(@RequestBody AddressAdd addressAdd) {
                Address address = new Address();
                BeanUtils.copyProperties(addressAdd, address);
                address.setUserId(tokenHolder.getUserId());
                addressService.update(address);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "修改成功");
                return Result.success(responseMap);
        }
        @PostMapping("/delete")
        public Result<Map<String, String>> delete(@RequestParam Long id){
                addressService.delete(id);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "删除成功");
                return Result.success(responseMap);
        }
        @GetMapping("/getdefault")
        public Result<Map<String, String>> getDefault(){
                Long userId = tokenHolder.getUserId();
                Address address = addressService.getDefault(userId);
               Map<String, String> responseMap = new HashMap<>();
               if(address==null){
                       responseMap.put("message", "获取成功");
                       responseMap.put("address",addressService.getAddressList(userId).get(0).toString());
                       return Result.success(responseMap);
               }
               responseMap.put("message", "获取成功");
               responseMap.put("address",address.toString());
               return Result.success(responseMap);
        }
        @GetMapping("/getaddressbyid")
        public Result<Address> getAddressById(@RequestParam Long addressId){
                Address address=addressService.getAddressById(addressId);
                return Result.success(address);
        }

}
