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

import java.util.ArrayList;
import java.util.List;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Row;

import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The write buffer
 */
public class GriddbBufferedRecords implements BufferedRecords {

    private static final Logger LOG = LoggerFactory.getLogger(GriddbBufferedRecords.class);
    private String containerName;
    private GriddbSinkConnectorConfig config;
    private DatabaseStructure dbStructure;
    private List<SinkRecord> records = new ArrayList<>();
    private List<Row> insertRows = new ArrayList<>();
    private FieldsMetadata fieldsMetadata;
    private Container<?, Row> container;
    private ContainerInfo containerInfo;
    private SchemaPair currentSchemaPair;

    /**
     * Contructor
     * @param config        the config parameters
     * @param containerName the container name
     * @param dbStructure   database structure object
     */
    public GriddbBufferedRecords(GriddbSinkConnectorConfig config, String containerName,
            DatabaseStructure dbStructure) {
        this.containerName = containerName;
        this.config = config;
        this.dbStructure = dbStructure;
    }

    /**
     * Add record to buffer
     * @param record : the record object
     * @return list sink record object are in buffer
     * @throws GSException
     */
    public List<SinkRecord> add(SinkRecord record) throws GSException {
        final SchemaPair schemaPair = new SchemaPair(
            record.keySchema(),
            record.valueSchema()
        );

        final List<SinkRecord> flushed = new ArrayList<>();

        LOG.info("Put 1 record to buffer of container {}", this.containerName);
        if (currentSchemaPair == null || !currentSchemaPair.equals(schemaPair)) {
            currentSchemaPair = schemaPair;
            flushed.addAll(flush());

            // Re-initialize everything that depends on the record schema
            // Get fields from record
            fieldsMetadata = FieldsMetadata.extract(containerName, record.keySchema(), record.valueSchema());

            dbStructure.createContainer(containerName, fieldsMetadata);
            this.container = dbStructure.getContainer(containerName);
            this.containerInfo = dbStructure.getContainerInfo(containerName);
        }
        Row row = dbStructure.createRow(this.container, this.containerInfo, fieldsMetadata, record);

        this.insertRows.add(row);

        records.add(record);

        if (records.size() >= config.batchSize) {
            flushed.addAll(flush());
        }
        return flushed;
    }

    /**
     * Write the data from buffer to database
     * @return list record are written to database
     * @throws GSException
     */
    public List<SinkRecord> flush() throws GSException {
        if (records.isEmpty()) {
            LOG.debug("Records is empty");
            return new ArrayList<>();
        }

        LOG.debug("Push all rows of Buffer {} to database", this.containerName);
        if (this.config.useMultiPut) {
            this.dbStructure.putRows(this.container, this.insertRows);
        } else {
            for (Row insertRow : this.insertRows) {
                this.dbStructure.putRow(this.container, insertRow);
            }
        }

        final List<SinkRecord> flushedRecords = records;
        records = new ArrayList<>();
        this.insertRows = new ArrayList<>();
        return flushedRecords;
    }

    /**
     * Release the resource when close buffer
     * @throws GSException
     */
    public void close() throws GSException {
        this.container.close();
    }
}
