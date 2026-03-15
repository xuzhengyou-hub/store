package com.yourname.mall.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserAddressRequest {

    @NotNull(message = "地址ID不能为空")
    private Long id;

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String receiverPhone;

    private String province;

    private String city;

    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    private Integer isDefault;
}

