package com.galapea.techblog.textstorage.controller;

import com.galapea.techblog.textstorage.entity.User;
import com.galapea.techblog.textstorage.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {
    private final UserService userService;

    @GetMapping
    String users(Model model) {
        List<User> users = userService.fetchAll();
        model.addAttribute("users", users);
        return "users";
    }
}
