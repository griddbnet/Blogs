package com.galapea.techblog.fitnesstracking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@GetMapping("/")
	public String main() {
		return "redirect:/workouts";
	}

}
