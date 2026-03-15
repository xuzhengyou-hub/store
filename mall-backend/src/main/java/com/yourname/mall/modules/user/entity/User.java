package com.yourname.mall.modules.user.entity;

import lombok.Data;

@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer status;
}
