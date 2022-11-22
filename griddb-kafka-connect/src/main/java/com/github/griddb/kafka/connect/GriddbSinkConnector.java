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

package com.github.griddb.kafka.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.griddb.kafka.connect.sink.GriddbSinkConnectorConfig;
import com.github.griddb.kafka.connect.sink.GriddbSinkTask;
import com.github.griddb.kafka.connect.util.Utility;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Griddb sink connector
 */
public class GriddbSinkConnector extends SinkConnector {
    private static final Logger log = LoggerFactory.getLogger(GriddbSinkConnector.class);
    private Map<String, String> configProps;
    private static final String DEFAULT_VERSION = "Unknown";

    /**
     * Start connector
     */
    @Override
    public void start(Map<String, String> props) {
        configProps = props;
    }

    /**
     * Stop connector
     */
    @Override
    public void stop() {
        // Do nothing
    }

    /**
     * Task config
     */
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        log.info("Setting task configurations for {} workers.", maxTasks);
        final List<Map<String, String>> configs = new ArrayList<>(maxTasks);
        for (int i = 0; i < maxTasks; i++) {
            configs.add(configProps);
        }
        return configs;
    }

    @Override
    public String version() {
        return Utility.getConfigString("sink_connector_version", DEFAULT_VERSION);
    }

    /**
     * The config parameters
     */
    @Override
    public ConfigDef config() {
        return GriddbSinkConnectorConfig.CONFIG_DEF;
    }

    /**
     * The task class
     */
    @Override
    public Class<? extends Task> taskClass() {
        return GriddbSinkTask.class;
    }

}