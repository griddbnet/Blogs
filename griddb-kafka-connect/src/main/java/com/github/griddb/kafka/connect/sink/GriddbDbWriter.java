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
import java.util.HashMap;
import java.util.Map;


import com.toshiba.mwcloud.gs.GSException;

import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Griddb database writer
 */
public class GriddbDbWriter implements DbWriter {
    private static final Logger LOG = LoggerFactory.getLogger(GriddbDbWriter.class);

    private final GriddbSinkConnectorConfig config;
    private final DatabaseStructure dbStructure;

    /**
     * The constructor method
     * @param config      the configuration parameters
     * @param dbStructure the database structure
     */
    public GriddbDbWriter(final GriddbSinkConnectorConfig config, DatabaseStructure dbStructure) {
        this.config = config;
        this.dbStructure = dbStructure;
    }

    /**
     * Write list records to database
     * @param records : list records
     * @throws GSException
     */
    public void write(Collection<SinkRecord> records) throws GSException {

        final Map<String, BufferedRecords> bufferByTable = new HashMap<>();
        for (SinkRecord record : records) {
            final String containerName = destinationContainer(record.topic());
            BufferedRecords buffer = bufferByTable.get(containerName);
            if (buffer == null) {
                buffer = new GriddbBufferedRecords(config, containerName, dbStructure);
                bufferByTable.put(containerName, buffer);
            }
            buffer.add(record);
        }
        for (Map.Entry<String, BufferedRecords> entry : bufferByTable.entrySet()) {
            String containerName = entry.getKey();
            BufferedRecords buffer = entry.getValue();
            LOG.debug("Flushing records in Griddb Writer for containerName: {}", containerName);
            buffer.flush();
            buffer.close();
        }
    }

    /**
     * The container name
     * @param topic
     * @return
     */
    private String destinationContainer(String topic) {
        return config.containerNameFormat.replace("${topic}", topic);
    }
}
