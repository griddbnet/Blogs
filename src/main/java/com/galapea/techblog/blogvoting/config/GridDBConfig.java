package com.galapea.techblog.blogvoting.config;

import com.galapea.techblog.blogvoting.Constant;
import com.galapea.techblog.blogvoting.entity.Blog;
import com.galapea.techblog.blogvoting.entity.User;
import com.galapea.techblog.blogvoting.entity.VoteMetrics;
import com.toshiba.mwcloud.gs.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GridDBConfig {

    @Value("${GRIDDB_NOTIFICATION_MEMBER}")
    private String notificationMember;

    @Value("${GRIDDB_CLUSTER_NAME}")
    private String clusterName;

    @Value("${GRIDDB_USER}")
    private String user;

    @Value("${GRIDDB_PASSWORD}")
    private String password;

    @Bean
    public GridStore gridStore() throws GSException {
        // Acquiring a GridStore instance
        Properties properties = new Properties();
        properties.setProperty("notificationMember", notificationMember);
        properties.setProperty("clusterName", clusterName);
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        GridStore store = GridStoreFactory.getInstance().getGridStore(properties);
        return store;
    }

    @Bean
    public Collection<String, User> userCollection(GridStore gridStore) throws GSException {
        Collection<String, User> collection = gridStore.putCollection("users", User.class);
        collection.createIndex("email");
        return collection;
    }

    @Bean
    public Collection<String, Blog> blogCollection(GridStore gridStore) throws GSException {
        Collection<String, Blog> collection = gridStore.putCollection("blogs", Blog.class);
        collection.createIndex("title");
        return collection;
    }

    @Bean
    public TimeSeries<VoteMetrics> voteMetricContainer(GridStore gridStore) throws GSException {
        TimeSeries<VoteMetrics> timeSeries = gridStore.putTimeSeries(Constant.VOTEMETRICS_CONTAINER, VoteMetrics.class);
        timeSeries.createIndex("blogId");
        timeSeries.createIndex("userId");
        return timeSeries;
    }

    public ContainerInfo voteMetricContainerInfo() throws GSException {
        ContainerInfo containerInfo = new ContainerInfo();
        List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();
        columnList.add(new ColumnInfo("timestamp", GSType.TIMESTAMP));
        columnList.add(new ColumnInfo("blogId", GSType.STRING));
        columnList.add(new ColumnInfo("userId", GSType.STRING));
        containerInfo.setColumnInfoList(columnList);
        containerInfo.setRowKeyAssigned(true);
        TimeSeriesProperties tsProp = new TimeSeriesProperties();
        tsProp.setRowExpiration(15, TimeUnit.MINUTE);
        tsProp.setExpirationDivisionCount(5);
        containerInfo.setTimeSeriesProperties(tsProp);
        return containerInfo;
    }
}
