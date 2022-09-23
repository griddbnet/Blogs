/**
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
 **/

package com.github.griddb.kafka.connect;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.griddb.kafka.connect.source.GriddbSourceConnectorConfig;
import com.github.griddb.kafka.connect.source.GriddbSourceTask;
import com.github.griddb.kafka.connect.util.Utility;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Griddb source connector
 */
public class GriddbSourceConnector extends SourceConnector {

    private static final Logger log = LoggerFactory.getLogger(GriddbSourceConnector.class);
    private Map<String, String> configProperties;
    private static final String DEFAULT_VERSION = "Unknown";

    /**
     * The config object
     */
    @Override
    public ConfigDef config() {
        return GriddbSourceConnectorConfig.CONFIG_DEF;
    }

    /**
     * The task class
     */
    @Override
    public Class<? extends Task> taskClass() {
        return GriddbSourceTask.class;
    }

    /**
     * Source connector starts
     * @param properties : the connector config properties
     */
    @Override
    public void start(Map<String, String> properties) throws ConnectException {
        configProperties = properties;
        log.info("Starting Griddb-Kafka Source Connector");
    }

    /**
     * Task configs
     * @maxTasks : max task
     */
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        Map<String, String> taskProps = new HashMap<>(configProperties);
        List<Map<String, String>> taskConfigs = Collections.singletonList(taskProps);
        return taskConfigs;
    }

    /**
     * Source connector stops
     */
    @Override
    public void stop() throws ConnectException {
        log.info("Stopping Griddb-Kafka Source Connector");
    }

    @Override
    public String version() {
        return Utility.getConfigString("source_connector_version", DEFAULT_VERSION);
    }
}
