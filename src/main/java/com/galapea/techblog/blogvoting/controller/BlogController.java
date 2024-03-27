package com.galapea.techblog.blogvoting.controller;

import com.galapea.techblog.blogvoting.model.CreateBlogRequest;
import com.galapea.techblog.blogvoting.service.BlogService;
import com.toshiba.mwcloud.gs.GSException;
import net.datafaker.Faker;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/blogs")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @ResponseBody
    @PostMapping(path = "/api", produces = MediaType.TEXT_HTML_VALUE)
    String create(@RequestBody CreateBlogRequest createBlogRequest) throws GSException {
        this.blogService.create(createBlogRequest);
        return "Created";
    }

    @GetMapping(path = "/new")
    String create(Model model) {
        CreateBlogRequest blog = new CreateBlogRequest();
        Faker faker = new Faker();
        blog.setTitle(faker.worldOfWarcraft().quotes());
        model.addAttribute("blog", blog);
        return "blog_form";
    }

    @PostMapping("/save")
    public String saveTutorial(CreateBlogRequest blog, RedirectAttributes redirectAttributes) {
        try {
            this.blogService.create(blog);

            redirectAttributes.addFlashAttribute("message", "The blog has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", e.getMessage());
        }

        return "redirect:/votes";
    }
}
