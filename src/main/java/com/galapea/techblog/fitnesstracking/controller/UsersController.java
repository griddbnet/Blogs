package com.galapea.techblog.fitnesstracking.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.galapea.techblog.fitnesstracking.entity.User;
import com.galapea.techblog.fitnesstracking.service.UserService;

import lombok.RequiredArgsConstructor;

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
