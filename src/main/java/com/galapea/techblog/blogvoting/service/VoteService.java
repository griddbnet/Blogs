package com.galapea.techblog.blogvoting.service;

import com.galapea.techblog.blogvoting.entity.VoteMetrics;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VoteService {
    private final Logger log = LoggerFactory.getLogger(VoteService.class);
    private final GridStore gridStore;
    private final BlogService blogService;
    private final VoteMetricService voteMetricService;

    public VoteService(GridStore gridStore, BlogService blogService, VoteMetricService voteMetricService) {
        this.gridStore = gridStore;
        this.blogService = blogService;
        this.voteMetricService = voteMetricService;
    }

    public void voteUp(String blogId, String userId) throws GSException {
        VoteMetrics metrics = voteMetricService.getVoted(blogId, userId);
        if (metrics == null) {
            voteMetricService.saveVote(blogId, userId, 1);
            blogService.updateVoteUp(blogId);
        }
    }

    public void voteDown(String blogId, String userId) throws GSException {
        VoteMetrics metrics = voteMetricService.getVoted(blogId, userId);
        if (metrics == null) {
            voteMetricService.saveVote(blogId, userId, 0);
            blogService.updateVoteDown(blogId);
        }
    }
}
