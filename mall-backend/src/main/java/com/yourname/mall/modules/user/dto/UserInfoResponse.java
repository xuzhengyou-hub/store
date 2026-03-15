package com.yourname.mall.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoResponse {

    private String username;
    private String phone;
    private String nickname;
    private String avatar;
}
