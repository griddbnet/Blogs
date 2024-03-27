package com.galapea.techblog.blogvoting;

import com.galapea.techblog.blogvoting.model.CreateBlogRequest;
import com.galapea.techblog.blogvoting.service.BlogService;
import com.toshiba.mwcloud.gs.GSException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupCommand implements CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(StartupCommand.class);
    private final BlogService blogService;

    public StartupCommand(BlogService blogService) {
        this.blogService = blogService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("run....");
        seedBlogs();
    }

    private void seedBlogs() throws GSException {
        List<String> titles = new ArrayList<>();
        titles.add("Building a Desktop WiFi Network Monitor with Tauri, React, Node.js, and GridDB");
        titles.add("Analyzing Social Media’s Correlation with Mental Health using GridDB");
        titles.add("Realtime Event Tracking using Spring Boot and GridDB");
        titles.add("Creating A Daily Meal Plan App Using ReactJS, NodeJS, ExpressJS, and GridDB");
        titles.add("A Python-Based IoT Data Dashboard");
        titles.add("Create A Machine Learning Model using GridDB");
        titles.add("Create A Deep Learning Model using GridDB");
        for (String title : titles) {
            if (blogService.fetchOne(title) == null) {
                CreateBlogRequest rq = new CreateBlogRequest();
                rq.setTitle(title);
                blogService.create(rq);
            }
        }
    }
}
