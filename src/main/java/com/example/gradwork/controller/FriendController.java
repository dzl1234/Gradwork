package com.example.gradwork.controller;

import com.example.gradwork.dto.UserStatusDTO;
import com.example.gradwork.model.Friendship;
import com.example.gradwork.model.User;
import com.example.gradwork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @PostMapping("/addByUsername/{username}")
    public ResponseEntity<?> addFriendByUsername(@PathVariable String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        try {
            User friend = userService.findByUsername(username);
            userService.addFriend(currentUser.getId(), friend.getId());
            return ResponseEntity.ok("添加成功");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("用户不存在");
        }
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
