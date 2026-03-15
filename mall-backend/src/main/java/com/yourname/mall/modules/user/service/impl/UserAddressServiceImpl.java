package com.yourname.mall.modules.user.service.impl;

import com.yourname.mall.exception.BusinessException;
import com.yourname.mall.modules.user.dto.AddUserAddressRequest;
import com.yourname.mall.modules.user.dto.UpdateUserAddressRequest;
import com.yourname.mall.modules.user.dto.UserAddressPageResponse;
import com.yourname.mall.modules.user.dto.UserAddressResponse;
import com.yourname.mall.modules.user.entity.UserAddress;
import com.yourname.mall.modules.user.mapper.UserAddressMapper;
import com.yourname.mall.modules.user.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private static final int DEFAULT_SIZE = 5;

    private final UserAddressMapper userAddressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Long userId, AddUserAddressRequest request) {
        int isDefault = normalizeIsDefault(request.getIsDefault());
        if (isDefault == 1) {
            userAddressMapper.clearDefaultByUserId(userId);
        }
        userAddressMapper.insertAddress(
            userId,
            normalizeText(request.getReceiverName()),
            normalizeText(request.getReceiverPhone()),
            normalizeNullableText(request.getProvince()),
            normalizeNullableText(request.getCity()),
            normalizeText(request.getDetailAddress()),
            isDefault
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, UpdateUserAddressRequest request) {
        UserAddress address = userAddressMapper.findByUserIdAndId(userId, request.getId());
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        int isDefault = normalizeIsDefault(request.getIsDefault());
        if (isDefault == 1) {
            userAddressMapper.clearDefaultByUserId(userId);
        }
        userAddressMapper.updateAddress(
            address.getId(),
            normalizeText(request.getReceiverName()),
            normalizeText(request.getReceiverPhone()),
            normalizeNullableText(request.getProvince()),
            normalizeNullableText(request.getCity()),
            normalizeText(request.getDetailAddress()),
            isDefault
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddressPageResponse listAddress(Long userId, Integer page, Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, 20);
        long total = userAddressMapper.countByUserId(userId);
        int totalPages = total == 0 ? 0 : (int) Math.ceil(total * 1.0 / safeSize);
        int offset = (safePage - 1) * safeSize;

        List<UserAddressResponse> list = userAddressMapper.queryPageByUserId(userId, safeSize, offset).stream()
            .map(address -> new UserAddressResponse(
                address.getId(),
                address.getReceiverName(),
                address.getReceiverPhone(),
                address.getProvince(),
                address.getCity(),
                address.getDetailAddress(),
                address.getIsDefault()
            ))
            .toList();
        return new UserAddressPageResponse(safePage, safeSize, total, totalPages, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOne(Long userId, Long addressId) {
        int affected = userAddressMapper.deleteByUserIdAndId(userId, addressId);
        if (affected == 0) {
            throw new BusinessException("地址不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long userId, List<Long> ids) {
        List<Long> validIds = ids.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (validIds.isEmpty()) {
            throw new BusinessException("无效的地址ID");
        }
        int affected = userAddressMapper.deleteByUserIdAndIds(userId, validIds);
        if (affected == 0) {
            throw new BusinessException("未删除任何地址");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long userId) {
        userAddressMapper.deleteAllByUserId(userId);
    }

    private String normalizeText(String raw) {
        if (raw == null) {
            throw new BusinessException("参数不能为空");
        }
        String normalized = raw.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("参数不能为空");
        }
        return normalized;
    }

    private String normalizeNullableText(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private int normalizeIsDefault(Integer isDefault) {
        if (isDefault == null) {
            return 0;
        }
        return isDefault == 1 ? 1 : 0;
    }
}

