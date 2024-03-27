package com.galapea.techblog.blogvoting.controller;

import com.galapea.techblog.blogvoting.entity.VoteMetrics;
import com.galapea.techblog.blogvoting.model.VoteAggregate;
import com.galapea.techblog.blogvoting.service.VoteMetricService;
import com.toshiba.mwcloud.gs.GSException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/httpbin")
public class HttpBinController {
    private static final Logger log = LoggerFactory.getLogger(HttpBinController.class);
    private final RestClient restClient;
    private final VoteMetricService voteMetricService;

    public HttpBinController(RestClient.Builder restClientBuilder, VoteMetricService voteMetricService) {
        restClient = restClientBuilder.baseUrl("https://httpbin.org/").build();
        this.voteMetricService = voteMetricService;
    }

    @GetMapping("/block/{seconds}")
    public String delay(@PathVariable int seconds) {
        ResponseEntity<Void> result =
                restClient.get().uri("/delay/" + seconds).retrieve().toBodilessEntity();

        log.info("{} on {}", result.getStatusCode(), Thread.currentThread());

        return Thread.currentThread().toString();
    }

    @GetMapping("/testCountMetrics")
    public String testCountMetrics() throws GSException {
        voteMetricService.testCountMetrics();
        return "Ok";
    }

    @GetMapping("/getAllMetrics")
    public List<VoteMetrics> testBiasa() {
        List<VoteMetrics> all = voteMetricService.getAllMetrics();
        log.info(all.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","))
                .toString());
        return all;
    }

    @GetMapping("/getVoteAggregateOverTime")
    public List<VoteAggregate> voteResult() {
        List<VoteAggregate> views = voteMetricService.getVoteAggregateOverTime();
        return views;
    }

    @PostMapping("/trigger")
    public String receptTrigger(@RequestParam String message) {
        log.info("Received Message : {}", message);
        return "Done";
    }
}
