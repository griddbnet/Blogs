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

import java.util.Collections;
import java.util.Map;

import com.github.griddb.kafka.connect.dialect.DbDialect;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Row;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BulkContainerQuerier: select all rows from container and use them to create
 * Kafka events
 */
public class BulkContainerQuerier extends ContainerQuerier {

    private static final Logger LOG = LoggerFactory.getLogger(BulkContainerQuerier.class);

    public BulkContainerQuerier(DbDialect dialect, String containerName, String topicPrefix) {
        super(dialect, containerName, topicPrefix);
    }

    /**
     * Create Kafka source event base on Griddb row
     */
    @Override
    public SourceRecord extractRecord() throws GSException {
        LOG.info("Extract record from Griddb Source Connector for container {}", this.containerName);
        Struct record = new Struct(schemaMapping.getSchema());
        Row row = this.getRow();
        schemaMapping.setStructRecordField(record, row);

        final String topic = this.topicPrefix + this.containerName;

        final Map<String, String> partition = Collections.singletonMap("container", this.containerName);
        return new SourceRecord(partition, null, topic, record.schema(), record);
    }

    /**
     * Create TQL statement to get data
     */
    @Override
    public String createQueryStatement() {
        return "SELECT *";
    }

}
