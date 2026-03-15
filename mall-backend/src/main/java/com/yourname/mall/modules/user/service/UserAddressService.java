package com.yourname.mall.modules.user.service;

import com.yourname.mall.modules.user.dto.AddUserAddressRequest;
import com.yourname.mall.modules.user.dto.UpdateUserAddressRequest;
import com.yourname.mall.modules.user.dto.UserAddressPageResponse;

import java.util.List;

public interface UserAddressService {

    void addAddress(Long userId, AddUserAddressRequest request);

    void updateAddress(Long userId, UpdateUserAddressRequest request);

    UserAddressPageResponse listAddress(Long userId, Integer page, Integer size);

    void deleteOne(Long userId, Long addressId);

    void deleteBatch(Long userId, List<Long> ids);

    void deleteAll(Long userId);
}

