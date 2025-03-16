package com.example.gradwork.controller;


import com.example.gradwork.dto.MessageDTO;
import com.example.gradwork.model.ChatMessage;
import com.example.gradwork.model.User;
import com.example.gradwork.service.ChatMessageService;
import com.example.gradwork.service.TranslationService;
import com.example.gradwork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private TranslationService translationService;

    @MessageMapping("/chat.send")
    public void processMessage(@Payload MessageDTO messageDTO) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User sender = userService.findByUsername(authentication.getName());
        User recipient = userService.findById(messageDTO.getRecipientId());

        String translatedContent = messageDTO.getContent();

        // 如果接收者的首选语言与发送者不同，执行翻译
        if (!recipient.getPreferredLanguage().equals(sender.getPreferredLanguage())) {
            translatedContent = translationService.translate(
                    messageDTO.getContent(),
                    sender.getPreferredLanguage(),
                    recipient.getPreferredLanguage()
            );
        }

        // 保存原始消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(sender.getId());
        chatMessage.setRecipientId(recipient.getId());
        chatMessage.setContent(messageDTO.getContent());
        chatMessage.setOriginalLanguage(sender.getPreferredLanguage());
        chatMessage.setSentAt(System.currentTimeMillis());
        chatMessageService.save(chatMessage);

        // 准备发送的DTO
        MessageDTO outgoingMessage = new MessageDTO();
        outgoingMessage.setSenderId(sender.getId());
        outgoingMessage.setSenderName(sender.getUsername());
        outgoingMessage.setRecipientId(recipient.getId());
        outgoingMessage.setContent(translatedContent);
        outgoingMessage.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(chatMessage.getSentAt()), ZoneId.systemDefault()));

        // 发送到WebSocket
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/messages",
                outgoingMessage
        );
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<MessageDTO>> getChatHistory(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        User otherUser = userService.findById(userId);

        List<ChatMessage> messages = chatMessageService.getChatHistory(currentUser.getId(), userId);

        // 转换为DTO
        List<MessageDTO> messageHistory = messages.stream()
                .map(message -> {
                    MessageDTO dto = new MessageDTO();
                    dto.setSenderId(message.getSenderId());
                    dto.setSenderName(message.getSenderId().equals(currentUser.getId()) ?
                            currentUser.getUsername() : otherUser.getUsername());
                    dto.setRecipientId(message.getRecipientId());

                    // 如果语言不同，翻译内容
                    String content = message.getContent();
                    if (!message.getOriginalLanguage().equals(currentUser.getPreferredLanguage())) {
                        content = translationService.translate(
                                message.getContent(),
                                message.getOriginalLanguage(),
                                currentUser.getPreferredLanguage()
                        );
                    }

                    dto.setContent(content);
                    dto.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getSentAt()), ZoneId.systemDefault()));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageHistory);
    }
}
