/*
 * Copyright (c) 2021 TOSHIBA Digital Solutions Corporation
 * Copyright 2018 Confluent Inc.
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

package com.github.griddb.kafka.connect.dialect;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.github.griddb.kafka.connect.sink.SinkRecordField;
import com.github.griddb.kafka.connect.source.TimestampIncrementingCriteria;
import com.github.griddb.kafka.connect.util.ColumnId;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;

/**
 * Interface class to working with database
 */
public interface DbDialect {

    /**
     * Get container info object
     * @param containerName the container name
     * @return ContainerInfo object
     * @throws GSException
     */
    ContainerInfo getContainerInfo(String containerName) throws GSException;

    /**
     * Get container
     * @param containerName the container name
     * @return Container object
     * @throws GSException
     */
    Container<?, Row> getContainer(String containerName) throws GSException;

    /**
     * Create container
     * @param containerName the container name
     * @param fields        the fields information get from Kafka
     * @return Container
     * @throws GSException
     */
    Container<?, Row> putContainer(String containerName, Collection<SinkRecordField> fields) throws GSException;

    /**
     * Get Griddb field type
     * @param field Kafka field object
     * @return Griddb type field
     */
    GSType getGsType(SinkRecordField field);

    /**
     * Create Griddb row
     * @param container the Griddb container object
     * @param row       the Griddb row object
     * @throws GSException
     */
    void putRow(Container<?, Row> container, Row row) throws GSException;

    /**
     * Crete Griddb rows
     * @param container the Griddb container object
     * @param rows      list rows
     * @throws GSException
     */
    void putRows(Container<?, Row> container, List<Row> rows) throws GSException;

    /**
     * Get RowSet object from tql
     * @param containerName the container name
     * @param tql           the TQL string
     * @return RowSet object
     * @throws GSException
     */
    RowSet<Row> getRowSet(String containerName, String tql) throws GSException;

    /**
     * Close the database dialect
     * @throws GSException
     */
    void close() throws GSException;

    /**
     * Get the current time on database
     * @return
     */
    Date currentTimeOnDB();

    /**
     * Get the TimestampIncrementingCriteria object for source connector
     * @param timestampColumns : list of timestamp column
     * @return
     */
    TimestampIncrementingCriteria criteriaFor(List<ColumnId> timestampColumns);
}
