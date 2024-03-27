package com.galapea.techblog.blogvoting.service;

import com.galapea.techblog.blogvoting.Constant;
import com.galapea.techblog.blogvoting.entity.VoteMetrics;
import com.galapea.techblog.blogvoting.model.VoteAggregate;
import com.toshiba.mwcloud.gs.Aggregation;
import com.toshiba.mwcloud.gs.AggregationResult;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.TimeSeries;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VoteMetricService {
    private final Logger log = LoggerFactory.getLogger(VoteMetricService.class);
    private final TimeSeries<VoteMetrics> voteMetricContainer;
    private final GridStore gridStore;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    SimpleDateFormat sdfChartLabelStart = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    SimpleDateFormat sdfChartLabelEnd = new SimpleDateFormat("HH:mm");

    @Value("${GRIDDB_NOTIFICATION_MEMBER}")
    private String notificationMember;

    @Value("${GRIDDB_USER}")
    private String user;

    @Value("${GRIDDB_PASSWORD}")
    private String password;

    @Value("${GRIDDB_CLUSTER_NAME}")
    private String clusterName;

    public VoteMetricService(TimeSeries<VoteMetrics> voteMetricContainer, GridStore gridStore) {
        this.voteMetricContainer = voteMetricContainer;
        this.gridStore = gridStore;
    }

    public void saveVote(String blogId, String userId, Integer voteType) throws GSException {
        VoteMetrics vote = new VoteMetrics();
        vote.setBlogId(blogId);
        vote.setUserId(userId);
        vote.setTimestamp(new Date());
        vote.setVoteType(voteType);
        log.info("saveVote user:{}", vote);
        voteMetricContainer.put(vote);
    }

    public VoteMetrics getVoted(String blogId, String userId) throws GSException {
        Query<VoteMetrics> query;
        query = voteMetricContainer.query(
                "select * from votemetrics where blogId='" + blogId + "' and userId='" + userId + "'");
        RowSet<VoteMetrics> rs = query.fetch();
        if (rs.hasNext()) {
            return rs.next();
        }
        return null;
    }

    public List<VoteMetrics> getAllMetrics() {
        List<VoteMetrics> all = new ArrayList<>();
        Query<VoteMetrics> query;
        try {
            query = voteMetricContainer.query("select * from votemetrics order by timestamp desc limit 50");
            RowSet<VoteMetrics> rs = query.fetch();
            while (rs.hasNext()) {
                VoteMetrics model = rs.next();
                all.add(model);
            }
            log.info("row count:{}", all.size());
        } catch (Exception e) {
            log.error("ErrorBiasa", e);
        }
        return all;
    }

    public void testCountMetrics() throws GSException {
        log.info("testCountMetrics");
        List<VoteAggregate> countAggregates = new ArrayList<>();
        Integer voteType = 1;
        String RANGE = "MINUTE";
        TimeSeries<Row> timeSeries = gridStore.getTimeSeries(Constant.VOTEMETRICS_CONTAINER);
        // Query<Row> query = container.query("SELECT timestamp, COUNT(voteType) FROM " + Constant.VOTEMETRICS_CONTAINER
        // + " ", Row.class);
        String tql = "SELECT timestamp, COUNT(voteType) FROM votemetrics WHERE voteType=" + voteType
                + " GROUP BY range (timestamp) EVERY (1, " + RANGE + ")";
        tql = "SELECT timestamp, COUNT(voteType) FROM votemetrics GROUP BY timestamp";
        log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        log.error(tql);
        log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        Query<Row> query = timeSeries.query(tql);
        RowSet<Row> rowSet = query.fetch();
        // Row row;
        // while (rowSet.hasNext()) {
        //     row = rowSet.next();
        //     log.info("voteType{}, {}, count:{}", voteType, row.getTimestamp(0), row.getInteger(1));
        // }
    }

    private java.util.Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }

    public List<VoteAggregate> getVoteAggregateOverTime() {
        List<VoteAggregate> views = new ArrayList<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime fromDate = endDate.minusDays(30);
        try (TimeSeries<VoteMetrics> timeSeries =
                gridStore().getTimeSeries(Constant.VOTEMETRICS_CONTAINER, VoteMetrics.class)) {
            for (LocalDateTime currentDate = fromDate;
                    currentDate.isBefore(endDate);
                    currentDate = currentDate.plusMinutes(15)) {
                java.util.Date start =
                        convertToDateViaInstant(currentDate.minusMinutes(15).withSecond(0));
                java.util.Date end = convertToDateViaInstant(currentDate.withSecond(59));
                AggregationResult aggregationResult = timeSeries.aggregate(start, end, "voteType", Aggregation.COUNT);
                Long count = aggregationResult.getLong();
                if (count.compareTo(0L) > 0) {
                    String label = sdfChartLabelStart.format(start) + " to " + sdfChartLabelEnd.format(end);
                    views.add(new VoteAggregate(convertToDateViaInstant(currentDate), count, label));
                }
            }
        } catch (GSException e) {
            log.error("Error getVoteAggregateOverTime", e);
        }
        return views;
    }

    private GridStore gridStore() throws GSException {
        // Acquiring a GridStore instance
        Properties properties = new Properties();
        properties.setProperty("notificationMember", notificationMember);
        properties.setProperty("clusterName", clusterName);
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        GridStore store = GridStoreFactory.getInstance().getGridStore(properties);
        return store;
    }
}
