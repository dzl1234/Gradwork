package com.example.gradwork.repository;

import com.example.gradwork.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.senderId = :userId1 AND m.recipientId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.recipientId = :userId1) " +
            "ORDER BY m.sentAt ASC")
    List<ChatMessage> findChatHistory(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE " +
            "m.recipientId = :userId AND m.isRead = false")
    long countUnreadMessages(@Param("userId") Long userId);

    @Query("UPDATE ChatMessage m SET m.delivered = true " +
            "WHERE m.recipientId = :userId AND m.delivered = false")
    void markAsDelivered(@Param("userId") Long userId);

    @Query("UPDATE ChatMessage m SET m.isRead = true " +
            "WHERE m.recipientId = :userId AND m.senderId = :senderId AND m.isRead = false")
    void markAsRead(@Param("userId") Long userId, @Param("senderId") Long senderId);
}
