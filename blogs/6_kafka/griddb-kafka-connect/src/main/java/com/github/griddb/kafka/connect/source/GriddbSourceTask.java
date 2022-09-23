/*
 * Copyright (c) 2021 TOSHIBA Digital Solutions Corporation
 * Copyright 2015 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.griddb.kafka.connect.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.griddb.kafka.connect.dialect.DbDialect;
import com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect;
import com.toshiba.mwcloud.gs.GSException;
import com.github.griddb.kafka.connect.util.Utility;

import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Griddb source task
 */
@SuppressWarnings("PMD.LongVariable")
public class GriddbSourceTask extends SourceTask {
    private static final String DEFAULT_VERSION = "Unknown";
    private static final Logger LOG = LoggerFactory.getLogger(GriddbSourceTask.class);
    private GriddbSourceConnectorConfig config;
    private Time time;
    private DbDialect dialect;
    private PriorityQueue<ContainerQuerier> containerQueue = new PriorityQueue<ContainerQuerier>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private int pollIntervalDefault;
    /**
     * The constructor method
     */
    public GriddbSourceTask() {
        this.time = new SystemTime();
    }

    /**
     * The constructor method with time input
     */
    public GriddbSourceTask(Time time) {
        this.time = time;
    }

    /**
     * Call actions to create Kafka topics and events
     */
    @Override
    @SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
    public List<SourceRecord> poll() throws InterruptedException {
        LOG.trace("{} Polling for new data");

        pollIntervalDefault = config.getInt(GriddbSourceConnectorConfig.POLLING_INTERVAL_CONFIG);

        while (running.get()) {
            final ContainerQuerier querier = containerQueue.peek();

            if (!querier.querying()) {
                // If not in the middle of an update, wait for next update time
                final long nextUpdate = querier.getLastUpdate() + pollIntervalDefault;
                final long now = time.milliseconds();
                final long sleepMs = Math.min(nextUpdate - now, 100);
                if (sleepMs > 0) {
                    LOG.trace("Waiting {} ms to poll {} next", nextUpdate - now, querier.toString());
                    time.sleep(sleepMs);
                    try {
                        querier.startQuery();
                    } catch (GSException e) {
                        LOG.error("Failed to run query for container {}: {}", querier.toString(), e);
                        resetAndRequeueHead(querier);
                        return null;
                    }
                    continue;
                }
            }

            final List<SourceRecord> results = new ArrayList<>();
            boolean hadNext = true;
            try {
                querier.startQuery();

                while (hadNext = querier.hasNext()) {
                    results.add(querier.extractRecord());
                }
            } catch (GSException e) {
                LOG.error("Failed to run query for container {}: {}", querier.toString(), e);
                resetAndRequeueHead(querier);
                return new ArrayList<>();
            }

            if (!hadNext) {
                // If we finished processing the results from the current query, we can reset
                // and send the querier to the tail of the queue
                resetAndRequeueHead(querier);
            }

            if (results.isEmpty()) {
                LOG.trace("No updates for {}", querier.toString());
                return results;
            }

            LOG.debug("Returning {} records for {}", results.size(), querier.toString());
            return results;
        }

        // Only in case of shutdown
        final ContainerQuerier containerquerier = containerQueue.peek();
        if (containerquerier != null) {
            resetAndRequeueHead(containerquerier);
        }
        closeResources();

        return new ArrayList<>();
    }

    /**
     * Start action
     * 
     * @param properties : properties from config file
     */
    @Override
    public void start(Map<String, String> properties) {
        LOG.info("Starting Griddb-Kafka source task");
        try {
            config = new GriddbSourceConnectorConfig(properties);
        } catch (ConfigException e) {
            throw new ConnectException("Couldn't start GriddbSourceTask due to configuration error", e);
        }

        Map<String, List<Map<String, String>>> partitionsByTableFqn = new HashMap<>();

        List<String> containers = config.getList(GriddbSourceConnectorConfig.CONTAINERS_CONFIG);

        if (containers.isEmpty()) {
            throw new ConnectException("Invalid configuration: each GriddbSourceConnectorConfig must have at "
                    + "least one container assigned to it");
        }

        dialect = new GriddbDatabaseDialect(config);
        LOG.info("Using Griddb dialect");

        String mode = config.getString(GriddbSourceConnectorConfig.MODE_CONFIG);

        Map<Map<String, String>, Map<String, Object>> offsets = null;

        List<Map<String, String>> partitions = new ArrayList<>(containers.size());
        for (String container : containers) {
            // Find possible partition maps for different offset protocols
            // We need to search by all offset protocol partition keys to support
            // compatibility
            List<Map<String, String>> tablePartitions = possibleContainerPartitions(container);
            partitions.addAll(tablePartitions);
            partitionsByTableFqn.put(container, tablePartitions);
        }
        if (context != null) {
            offsets = context.offsetStorageReader().offsets(partitions);
        }
        LOG.trace("The partition offsets are {}", offsets);

        String topicPrefix = config.getString(GriddbSourceConnectorConfig.TOPIC_PREFIX_CONFIG);

        List<String> timestampColumns = config.getList(GriddbSourceConnectorConfig.TIMESTAMP_COLUMN_NAME_CONFIG);

        for (String container : containers) {

            final List<Map<String, String>> tablePartitionsToCheck;

            tablePartitionsToCheck = partitionsByTableFqn.get(container);

            Map<String, Object> offset = null;
            if (offsets != null) {
                for (Map<String, String> toCheckPartition : tablePartitionsToCheck) {
                    offset = offsets.get(toCheckPartition);
                    if (offset != null) {
                        LOG.info("Found offset {} for partition {}", offsets, toCheckPartition);
                        break;
                    }
                }
            }

            if (mode.equals(GriddbSourceConnectorConfig.MODE_BULK)) {
                containerQueue.add(new BulkContainerQuerier(dialect, container, topicPrefix));
            } else {
                try {
                    containerQueue.add(
                            new TimestampContainerQuerier(dialect, container, topicPrefix, timestampColumns, offset));
                } catch (GSException e) {
                    throw new ConnectException("Error when create new TimestampContainerQuerier object", e);
                }
            }
        }

        running.set(true);

    }

    /**
     * Stop action
     */
    @Override
    public void stop() {
        running.set(false);
    }

    /**
     * Griddb source task version
     */
    @Override
    public String version() {
        return Utility.getConfigString("source_task_version", DEFAULT_VERSION);
    }

    /**
     * Set the ContainerQuerier object to the end of queue
     * 
     * @param expectedHead : the ContainerQuerier in the head
     */
    private void resetAndRequeueHead(ContainerQuerier expectedHead) {
        LOG.debug("Resetting querier {}", expectedHead.toString());
        ContainerQuerier removedQuerier = containerQueue.poll();
        assert removedQuerier == expectedHead;
        expectedHead.reset(time.milliseconds());
        containerQueue.add(expectedHead);
    }

    /**
     * Close the database dialect
     */
    protected void closeResources() {
        LOG.info("Closing resources for Griddb source task");
        if (dialect == null) {
            return;
        }
        try {
            dialect.close();
        } catch (GSException e) {
            LOG.warn("Error while closing the Griddb dialect: ", e);
        } finally {
            dialect = null;
        }
    }

    /**
     * This method returns a list of possible partition maps for different offset
     * protocols
     */
    private List<Map<String, String>> possibleContainerPartitions(String container) {
        return Arrays.asList(
                OffsetProtocols.sourcePartitionForProtocolV1(container),
                OffsetProtocols.sourcePartitionForProtocolV0(container));
    }

}
