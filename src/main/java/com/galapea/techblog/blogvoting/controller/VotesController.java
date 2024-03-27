package com.galapea.techblog.blogvoting.controller;

import com.galapea.techblog.blogvoting.entity.Blog;
import com.galapea.techblog.blogvoting.service.BlogService;
import com.galapea.techblog.blogvoting.service.KeyGenerator;
import com.galapea.techblog.blogvoting.service.VoteMetricService;
import com.galapea.techblog.blogvoting.service.VoteService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/votes")
public class VotesController {
    private static final String LAST_EVENT_ID = "Last-Event-ID";
    private static final Logger log = LoggerFactory.getLogger(VotesController.class);
    private final BlogService blogService;
    private final VoteService voteService;
    private final VoteMetricService voteMetricService;
    private String eventId = UUID.randomUUID().toString();

    public VotesController(BlogService blogService, VoteService voteService, VoteMetricService voteMetricService) {
        this.blogService = blogService;
        this.voteService = voteService;
        this.voteMetricService = voteMetricService;
    }

    @GetMapping
    String votes(Model model) {
        List<Blog> blogs = blogService.fetchAll();
        model.addAttribute("blogs", blogs);
        return "votes";
    }

    @GetMapping("/up/{id}")
    public String voteUp(@PathVariable("id") String blogId, Model model, RedirectAttributes redirectAttributes) {
        try {
            String userId = KeyGenerator.next("us");
            voteService.voteUp(blogId, userId);
            redirectAttributes.addFlashAttribute("message", "Voting successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Oh no!");
        }
        return "redirect:/votes";
    }

    @GetMapping("/down/{id}")
    public String voteDown(@PathVariable("id") String blogId, Model model, RedirectAttributes redirectAttributes) {
        try {
            String userId = KeyGenerator.next("us");
            voteService.voteDown(blogId, userId);
            redirectAttributes.addFlashAttribute("message", "Voting successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Oh no!");
        }
        return "redirect:/votes";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("aggregates", voteMetricService.getVoteAggregateOverTime());
        return "vote_dashboard";
    }

    @GetMapping("/realtimedashboard")
    public String realtimedashboard(Model model) {
        model.addAttribute("aggregates", voteMetricService.getVoteAggregateOverTime());
        return "vote_dashboard_realtime";
    }

    @GetMapping("/chart-data")
    public SseEmitter streamSseMvc(@RequestHeader Map<String, String> headers) {
        log.info("LAST_EVENT_ID=", headers.get(LAST_EVENT_ID));
        if (headers.containsKey(LAST_EVENT_ID) && headers.get(LAST_EVENT_ID).equals(eventId)) {
            eventId = headers.get(LAST_EVENT_ID);
        }
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(15).toMillis());
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        sseMvcExecutor.execute(() -> {
            try {
                for (int i = 0; true; i++) {
                    log.info(">>" + i);
                    SseEventBuilder event = SseEmitter.event()
                            .data(voteMetricService.getVoteAggregateOverTime())
                            .id(eventId);
                    emitter.send(event);
                    Thread.sleep(Duration.ofSeconds(10).toMillis());
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
                log.error("exception SSE", ex);
            }
            emitter.complete();
        });
        return emitter;
    }
}
