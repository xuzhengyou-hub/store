package com.yourname.mall.modules.user.service;

public interface UserService {

    String login(String username, String password);

    void register(String username, String password);
}
