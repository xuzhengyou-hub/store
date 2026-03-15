package com.yourname.mall.modules.user.service;

import com.yourname.mall.modules.user.dto.UserInfoResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    String login(String username, String password);

    void register(String username, String password);

    UserInfoResponse getUserInfoByToken(String token);

    UserInfoResponse updateNicknameByToken(String token, String nickname);

    UserInfoResponse bindPhoneByToken(String token, String phone, String verificationCode);

    UserInfoResponse uploadAvatarByToken(String token, MultipartFile file);

    Resource loadAvatar(String fileName);
}
