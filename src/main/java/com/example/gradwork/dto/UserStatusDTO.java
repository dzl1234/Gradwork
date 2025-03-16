package com.example.gradwork.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户状态数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusDTO {
    private Long id;
    private String username;
    private String email;
    private String preferredLanguage;
    private boolean online;
    private String lastSeen; // 最后在线时间

    public void setUserId(Long id) {
    }
}
