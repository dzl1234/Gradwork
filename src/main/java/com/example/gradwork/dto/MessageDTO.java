package com.example.gradwork.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 消息数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    @Getter
    private Long receiverId;
    private String receiverName;
    private String originalContent; // 原始内容
    private String translatedContent; // 翻译后内容
    private String originalLanguage; // 原始语言
    private String targetLanguage; // 目标语言
    private LocalDateTime timestamp;
    private boolean read;


    public Long getRecipientId() {
        return receiverId;
    }

    public void setRecipientId(Long recipientId) {
        this.receiverId = recipientId;
    }

    public String getContent() {
        return originalContent;
    }

    public void setContent(String content) {
        this.originalContent = content;
    }
}
