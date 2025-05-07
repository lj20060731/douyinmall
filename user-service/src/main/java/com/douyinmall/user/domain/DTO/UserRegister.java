package com.douyinmall.user.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegister {
        private String username;
        private String image;
        private String password;
        private String phone;
        private int sex;
        private String email;
        private String idNumber;
}
