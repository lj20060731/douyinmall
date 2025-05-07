package com.douyinmall.address.mapper;


import com.douyinmall.address.domain.VO.Address;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressMapper {
        @Insert("insert into address values(null,#{userId},#{consignee},#{sex},#{phone},#{provinceName},#{cityName},#{districtName},#{detail},#{isDefault})")
        void addAddress(Address address);
        @Select("select * from address where user_id = #{userId}")
        List<Address> getAddressList(Long userId);

        void update(Address address);
        @Delete("DELETE FROM address WHERE id = #{id}")
        void delete(Long id);
        @Select("SELECT * FROM address WHERE user_id = #{userId} AND is_default = 1")
        Address getDefault(Long userId);

        @Select("SELECT * FROM address WHERE id = #{id}")
        Address getById(Long id);
}
