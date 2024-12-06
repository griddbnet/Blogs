package com.galapea.techblog.textstorage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String main() {
        return "redirect:/snippets";
    }
}
