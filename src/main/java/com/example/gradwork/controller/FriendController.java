package com.example.gradwork.controller;

import com.example.gradwork.dto.UserStatusDTO;
import com.example.gradwork.model.Friendship;
import com.example.gradwork.model.User;
import com.example.gradwork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserStatusDTO>> getAllFriends() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());

        List<Friendship> friendships = userService.getFriendships(currentUser.getId());

        List<UserStatusDTO> friendsList = friendships.stream()
                .map(friendship -> {
                    Long friendId = friendship.getUserId1().equals(currentUser.getId()) ?
                            friendship.getUserId2() : friendship.getUserId1();
                    User friend = userService.findById(friendId);

                    UserStatusDTO dto = new UserStatusDTO();
                    dto.setUserId(friend.getId());
                    dto.setUsername(friend.getUsername());
                    dto.setPreferredLanguage(friend.getPreferredLanguage());
                    dto.setOnline(userService.isUserOnline(friend.getId()));

                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(friendsList);
    }

    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addFriend(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());

        // 验证用户存在
        User friend = userService.findById(userId);
        if (friend == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        // 验证不是自己
        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.badRequest().body("不能添加自己为好友");
        }

        // 验证不是已经是好友
        if (userService.areFriends(currentUser.getId(), userId)) {
            return ResponseEntity.badRequest().body("已经是好友关系");
        }

        // 添加好友关系
        userService.addFriend(currentUser.getId(), userId);

        return ResponseEntity.ok("好友添加成功");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());

        // 验证是好友关系
        if (!userService.areFriends(currentUser.getId(), userId)) {
            return ResponseEntity.badRequest().body("不是好友关系");
        }

        // 移除好友关系
        userService.removeFriend(currentUser.getId(), userId);

        return ResponseEntity.ok("好友移除成功");
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserStatusDTO>> searchUsers(@RequestParam String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());

        List<User> users = userService.searchUsers(keyword);

        // 过滤当前用户和已是好友的用户
        List<UserStatusDTO> searchResults = users.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .filter(user -> !userService.areFriends(currentUser.getId(), user.getId()))
                .map(user -> {
                    UserStatusDTO dto = new UserStatusDTO();
                    dto.setUserId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setPreferredLanguage(user.getPreferredLanguage());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(searchResults);
    }
}
