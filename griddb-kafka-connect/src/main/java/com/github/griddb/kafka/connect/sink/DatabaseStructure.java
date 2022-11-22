/*
 * Copyright (c) 2021 TOSHIBA Digital Solutions Corporation
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

import java.util.List;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Row;

import org.apache.kafka.connect.sink.SinkRecord;

/**
 * The interface of database structure
 */
public interface DatabaseStructure {

    /**
     * Create container
     * @param config         the config parameters
     * @param containerName  the container name
     * @param fieldsMetadata Kafka fields information
     * @throws GSException
     */
    void createContainer(String containerName, FieldsMetadata fieldsMetadata) throws GSException;

    /**
     * Get container
     * @param containerName the container name
     * @return              container object
     * @throws GSException
     */
    Container<?, Row> getContainer(String containerName) throws GSException;

    /**
     * Get container info
     * @param containerName the container name
     * @return              container object
     * @throws GSException
     */
    ContainerInfo getContainerInfo(String containerName) throws GSException;

    /**
     * Create row
     * @param container      the Container object
     * @param containerInfo  the ContainerInfo object
     * @param fieldsMetadata Kafka fields object
     * @param record         The sink record
     * @return               the Griddb row
     * @throws GSException
     */
    Row createRow(Container<?, Row> container, ContainerInfo containerInfo, FieldsMetadata fieldsMetadata,
            SinkRecord record) throws GSException;

    /**
     * Put row to database
     * @param container the container object
     * @param row       the row
     * @throws GSException
     */
    void putRow(Container<?, Row> container, Row row) throws GSException;

    /**
     * Put multi rows to database
     * @param container the container
     * @param rows      list rows
     * @throws GSException
     */
    void putRows(Container<?, Row> container, List<Row> rows) throws GSException;

}
