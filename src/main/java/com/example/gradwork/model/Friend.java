package com.example.gradwork.model;

import jakarta.persistence.*;

@Entity
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 不需要定义 userId 字段，直接使用 ManyToOne 映射
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // 如果 friendId 是外键关联到 User 表的字段，确保映射友谊关系
    @ManyToOne
    @JoinColumn(name = "friend_id", insertable = false, updatable = false)
    private User friend;

    public Friend() {}

    // Getters and Setters
}

