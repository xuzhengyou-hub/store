package com.yourname.mall.modules.user.service.impl;

import com.yourname.mall.exception.BusinessException;
import com.yourname.mall.modules.user.dto.UserInfoResponse;
import com.yourname.mall.modules.user.entity.User;
import com.yourname.mall.modules.user.mapper.UserMapper;
import com.yourname.mall.modules.user.service.AvatarStorageService;
import com.yourname.mall.modules.user.service.JwtTokenService;
import com.yourname.mall.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String MOCK_SMS_CODE = "1111";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AvatarStorageService avatarStorageService;

    @Override
    @Transactional(readOnly = true)
    public String login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        User user = userMapper.findByUsername(normalizedUsername);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return jwtTokenService.generateToken(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        if (userMapper.existsByUsername(normalizedUsername)) {
            throw new BusinessException("用户名已被注册");
        }
        String encodedPassword = passwordEncoder.encode(password);
        userMapper.insertUser(normalizedUsername, encodedPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfoByToken(String token) {
        User user = getUserByToken(token);
        return buildUserInfo(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateNicknameByToken(String token, String nickname) {
        User user = getUserByToken(token);
        String normalizedNickname = normalizeNickname(nickname);
        userMapper.updateNickname(user.getId(), normalizedNickname);
        user.setNickname(normalizedNickname);
        return buildUserInfo(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse bindPhoneByToken(String token, String phone, String verificationCode) {
        User user = getUserByToken(token);

        String normalizedPhone = normalizePhone(phone);
        if (!MOCK_SMS_CODE.equals(verificationCode)) {
            throw new BusinessException("验证码错误");
        }

        User phoneOwner = userMapper.findByPhone(normalizedPhone);
        if (phoneOwner != null && !phoneOwner.getId().equals(user.getId())) {
            throw new BusinessException("手机号已被绑定");
        }

        userMapper.updatePhone(user.getId(), normalizedPhone);
        user.setPhone(normalizedPhone);
        return buildUserInfo(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse uploadAvatarByToken(String token, MultipartFile file) {
        User user = getUserByToken(token);
        String fileName = avatarStorageService.save(file);
        String avatarUrl = "/api/user/avatar/" + fileName;

        userMapper.updateAvatar(user.getId(), avatarUrl);
        user.setAvatar(avatarUrl);
        return buildUserInfo(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadAvatar(String fileName) {
        return avatarStorageService.load(fileName);
    }

    private User getUserByToken(String token) {
        Long userId = jwtTokenService.parseUserId(token);
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在或已删除");
        }
        return user;
    }

    private UserInfoResponse buildUserInfo(User user) {
        return new UserInfoResponse(
            user.getUsername(),
            user.getPhone(),
            user.getNickname(),
            user.getAvatar()
        );
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new BusinessException("用户名不能为空");
        }
        String normalized = username.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        return normalized;
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null) {
            throw new BusinessException("昵称不能为空");
        }
        String normalized = nickname.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("昵称不能为空");
        }
        if (normalized.length() > 64) {
            throw new BusinessException("昵称长度不能超过64个字符");
        }
        return normalized;
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            throw new BusinessException("手机号不能为空");
        }
        String normalized = phone.trim();
        if (!normalized.matches("^1\\d{10}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        return normalized;
    }
}
