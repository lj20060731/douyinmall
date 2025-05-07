package com.douyinmall.user.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdate {

        private String username;

        private String image;

        private String password;

        private String phone;

        private Integer sex;

        private String email;

        private String idNumber;

        private LocalDateTime createTime;
}
