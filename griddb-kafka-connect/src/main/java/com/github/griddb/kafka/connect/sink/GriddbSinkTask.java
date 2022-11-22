/*
 * Copyright (c) 2021 TOSHIBA Digital Solutions Corporation
 * Copyright 2016 Confluent Inc.
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

package com.github.griddb.kafka.connect.sink;

import java.util.Collection;
import java.util.Map;

import com.github.griddb.kafka.connect.dialect.DbDialect;
import com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect;
import com.github.griddb.kafka.connect.util.Utility;
import com.toshiba.mwcloud.gs.GSException;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Griddb sink task
 */
public class GriddbSinkTask extends SinkTask {

    private DbDialect dialect;
    private DbWriter writer;
    private GriddbSinkConnectorConfig config;

    private static final Logger LOG = LoggerFactory.getLogger(GriddbSinkTask.class);
    private static final String DEFAULT_VERSION = "Unknown";

    /**
     * put records to Griddb
     * @param records : list sink records
     */
    @Override
    public void put(Collection<SinkRecord> records) {
        LOG.info("Put records to GridDB with number records {}", records.size());
        if (records.isEmpty()) {
            return;
        }
        final SinkRecord first = records.iterator().next();
        final int recordsCount = records.size();
        LOG.debug(
                "Received {} records. First record kafka coordinates:({}-{}-{}). Writing them to the " + "database...",
                recordsCount, first.topic(), first.kafkaPartition(), first.kafkaOffset());

        try {
            writer.write(records);
        } catch (GSException ex) {
            LOG.info("GSException with error message {}, error code {}", ex.getMessage(), ex.getErrorCode());
            throw new ConnectException(ex);
        }

    }

    /**
     * Stop
     */
    @Override
    public void stop() {
        // Do nothing
    }

    /**
     * Sink task version
     */
    public String version() {
        return Utility.getConfigString("sink_task_version", DEFAULT_VERSION);
    }

    /**
     * Sink task starts
     */
    @Override
    public void start(final Map<String, String> props) {
        LOG.info("Starting GridDB Sink task");
        config = new GriddbSinkConnectorConfig(props);
        initWriter();
    }

    /**
     * Create GriddbDb Writer
     */
    private void initWriter() {
        dialect = new GriddbDatabaseDialect(config);
        final DatabaseStructure dbStructure = new GriddbDbStructure(dialect);
        LOG.info("Initializing writer using GridDB dialect: {}", dialect.getClass().getSimpleName());
        writer = new GriddbDbWriter(config, dbStructure);
    }

    /**
     * Flush data
     */
    @Override
    public void flush(Map<TopicPartition, OffsetAndMetadata> map) {
        // Do nothing
    }

}
