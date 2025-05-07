package com.douyinmall.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.douyinmall.user.domain.DTO.UserLogin;
import com.douyinmall.user.domain.VO.User;
import com.douyinmall.user.mapper.UserMapper;
import com.douyinmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UserServiceImpl implements UserService {
        @Autowired
        private UserMapper userMapper;
        @Override
        public User login(UserLogin userLogin) {
                // 对用户输入的密码进行 MD5 加密
                String encryptedPassword = md5Encrypt(userLogin.getPassword());

                // 创建查询条件
                QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                        .select("*")
                        .eq("username", userLogin.getUsername())
                        .eq("password", encryptedPassword);

                // 执行查询
                return userMapper.selectOne(queryWrapper);
        }


        private String md5Encrypt(String input) {
                try {
                        // 获取 MD5 算法实例
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        // 将输入的字符串转换为字节数组
                        byte[] messageDigest = md.digest(input.getBytes());

                        // 将字节数组转换为十六进制字符串
                        StringBuilder hexString = new StringBuilder();
                        for (byte b : messageDigest) {
                                String hex = Integer.toHexString(0xFF & b);
                                if (hex.length() == 1) {
                                        hexString.append('0');
                                }
                                hexString.append(hex);
                        }
                        return hexString.toString();
                } catch (NoSuchAlgorithmException e) {
                        // 处理异常
                        throw new RuntimeException(e);
                }
        }
        @Override
        public User getInfo(Long id) {
                return userMapper.selectById(id);
        }

        @Override
        public void register(User user) {
                String encryptedPassword = md5Encrypt(user.getPassword());
                user.setPassword(encryptedPassword);
                userMapper.insert(user);
        }

        @Override
        public void update(User user) {
                userMapper.updateById(user);
        }
}
