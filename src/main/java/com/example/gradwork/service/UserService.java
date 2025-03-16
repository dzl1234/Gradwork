package com.example.gradwork.service;

import com.example.gradwork.model.Friendship;
import com.example.gradwork.model.User;
import com.example.gradwork.repository.FriendRepository;
import com.example.gradwork.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 在线用户缓存
    private final Set<Long> onlineUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Transactional
    public User save(User user) {
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.searchByUsername(keyword);
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId);
    }

    // 好友关系管理
    public List<Friendship> getFriendships(Long userId) {
        return friendRepository.findFriendshipsByUserId(userId);
    }

    public boolean areFriends(Long userId1, Long userId2) {
        return friendRepository.existsFriendship(userId1, userId2);
    }

    @Transactional
    public void addFriend(Long userId1, Long userId2) {
        // 确保用户ID按升序存储，便于查询
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);

        if (!friendRepository.existsFriendship(smallerId, largerId)) {
            Friendship friendship = new Friendship();
            friendship.setUserId1(smallerId);
            friendship.setUserId2(largerId);
            friendRepository.save(friendship);
        }
    }

    @Transactional
    public void removeFriend(Long userId1, Long userId2) {
        friendRepository.findFriendship(userId1, userId2)
                .ifPresent(friendship -> friendRepository.delete(friendship));
    }

    // 用户在线状态管理
    public void userConnected(Long userId) {
        onlineUsers.add(userId);
    }

    public void userDisconnected(Long userId) {
        onlineUsers.remove(userId);
    }

    public boolean isUserOnline(Long userId) {
        return onlineUsers.contains(userId);
    }

    public Set<Long> getOnlineUsers() {
        return Collections.unmodifiableSet(onlineUsers);
    }
}
