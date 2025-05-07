package com.douyinmall.user.service;


import com.douyinmall.user.domain.DTO.UserLogin;
import com.douyinmall.user.domain.VO.User;

public interface UserService {
        User login(UserLogin userLogin);

        User getInfo(Long id);

        void register(User user);

        void update(User user);
}
