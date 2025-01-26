package com.example.gradwork.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    // 简单的认证方法：此处可以加上实际的认证逻辑
    public boolean authenticate(String username, String password) {
        // 例如：检查用户名和密码是否匹配
        // 假设用户名和密码是固定的
        if ("admin".equals(username) && "password".equals(password)) {
            return true;  // 登录成功
        }
        return false;  // 登录失败
    }

    // 简单的注册方法：此处可以加上实际的注册逻辑
    public boolean register(String username, String password) {
        // 假设注册成功
        // 你可以在这里进行用户信息存储等操作
        return true;  // 注册成功
    }
}

