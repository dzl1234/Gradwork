package com.example.gradwork.controller;

import com.example.gradwork.model.User;
import com.example.gradwork.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        if (authService.authenticate(username, password)) {
            return "redirect:/chat";
        } else {
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        if (authService.register(username, password)) {
            return "redirect:/login?success=true";
        } else {
            return "redirect:/register?error=true";
        }
    }
}
