package com.yourname.mall.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAddressResponse {

    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String detailAddress;
    private Integer isDefault;
}

