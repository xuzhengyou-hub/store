package com.yourname.mall.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserAddressPageResponse {

    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPages;
    private List<UserAddressResponse> list;
}

