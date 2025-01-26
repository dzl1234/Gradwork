package com.example.gradwork.controller;


import com.example.gradwork.model.Message;
import com.example.gradwork.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @PostMapping("/sendMessage")
    public String sendMessage(@RequestParam String message) {
        chatService.sendMessage(message);
        return "redirect:/chat";
    }
}

