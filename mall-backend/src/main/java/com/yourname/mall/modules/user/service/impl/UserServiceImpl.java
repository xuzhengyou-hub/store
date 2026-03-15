package com.yourname.mall.modules.user.service.impl;

import com.yourname.mall.exception.BusinessException;
import com.yourname.mall.modules.user.entity.User;
import com.yourname.mall.modules.user.mapper.UserMapper;
import com.yourname.mall.modules.user.service.JwtTokenService;
import com.yourname.mall.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    @Transactional(readOnly = true)
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return jwtTokenService.generateToken(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        if (userMapper.existsByUsername(username)) {
            throw new BusinessException("用户名已被注册");
        }
        String encodedPassword = passwordEncoder.encode(password);
        userMapper.insertUser(username, encodedPassword);
    }
}
