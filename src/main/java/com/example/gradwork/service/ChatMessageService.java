package com.example.gradwork.service;

import com.example.gradwork.model.ChatMessage;
import com.example.gradwork.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatHistory(Long userId1, Long userId2) {
        return chatMessageRepository.findChatHistory(userId1, userId2);
    }

    public long countUnreadMessages(Long userId) {
        return chatMessageRepository.countUnreadMessages(userId);
    }

    @Transactional
    public void markMessagesAsDelivered(Long userId) {
        chatMessageRepository.markAsDelivered(userId);
    }

    @Transactional
    public void markMessagesAsRead(Long userId, Long senderId) {
        chatMessageRepository.markAsRead(userId, senderId);
    }
}
