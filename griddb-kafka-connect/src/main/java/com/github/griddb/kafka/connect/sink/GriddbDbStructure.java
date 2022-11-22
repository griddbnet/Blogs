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

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import com.github.griddb.kafka.connect.dialect.DbDialect;
import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.Row;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Griddb database structure
 */
public class GriddbDbStructure implements DatabaseStructure {
    private static final Logger LOG = LoggerFactory.getLogger(GriddbDbStructure.class);

    private DbDialect dialect;

    @Override
    public Container<?, Row> getContainer(String containerName) throws GSException {
        return this.dialect.getContainer(containerName);
    }

    public GriddbDbStructure(DbDialect dialect) {
        this.dialect = dialect;
    }

    /**
     * Create container
     * @param config         the config parameters
     * @param containerName  the container name
     * @param fieldsMetadata Kafka fields information
     * @throws GSException
     */
    @Override
    public void createContainer(String containerName, FieldsMetadata fieldsMetadata)
            throws GSException {
        ContainerInfo containerInfo = this.getContainerInfo(containerName);
        if (containerInfo == null) {
            LOG.debug("Not found container with name {}", containerName);
            create(containerName, fieldsMetadata);
        } else {
            LOG.debug("Found container with name {}", containerName);
        }
    }

    /**
     * Get container info
     * @param containerName the container name
     * @return              container object
     * @throws GSException
     */
    @Override
    public ContainerInfo getContainerInfo(String containerName) throws GSException {
        return this.dialect.getContainerInfo(containerName);
    }

    /**
     * Create row
     * @param container      the Container object
     * @param containerInfo  the ContainerInfo object
     * @param fieldsMetadata Kafka fields object
     * @param record         The sink record
     * @return               the Griddb row
     * @throws GSException
     */
    @Override
    public Row createRow(Container<?, Row> container, ContainerInfo containerInfo, FieldsMetadata fieldsMetadata,
            SinkRecord record) throws GSException {
        int columnCount = containerInfo.getColumnCount();
        Map<String, SinkRecordField> allFields = fieldsMetadata.getAllFields();
        if (allFields.values().size() != columnCount) {
            throw new GSException("Number fields of Kafka record is different with container");
        }
        Row row = container.createRow();
        int index = 0;

        for (String fieldName : fieldsMetadata.getFieldNames()) {
            final Field field = record.valueSchema().field(fieldName);
            ColumnInfo columnInfo = containerInfo.getColumnInfo(index);
            GSType gsType = columnInfo.getType();
            Object value = ((Struct) record.value()).get(field);
            if (value == null) {
                row.setNull(index);
                continue;
            }
            switch (gsType) {
                case STRING:
                    row.setString(index, (String) value);
                    break;
                case BOOL:
                    row.setBool(index, (Boolean) value);
                    break;
                case BYTE:
                    row.setByte(index, (Byte) value);
                    break;
                case SHORT:
                    row.setShort(index, (Short) value);
                    break;
                case INTEGER:
                    row.setInteger(index, (Integer) value);
                    break;
                case LONG:
                    row.setLong(index, (Long) value);
                    break;
                case FLOAT:
                    Float floatValue = (Float) value;
                    row.setFloat(index, floatValue.floatValue());
                    break;
                case DOUBLE:
                    Double doubleValue = (Double) value;
                    row.setDouble(index, doubleValue.doubleValue());
                    break;
                case TIMESTAMP:
                    Date dateObject = null;
                    try {
                        dateObject = (Date) value;
                        System.out.print(dateObject);
                    } catch (ClassCastException e) {
                        throw new GSException(e);
                    }
                    row.setTimestamp(index, dateObject);
                    break;
                case BLOB:
                    final byte[] bytes = (byte[]) value;
                    Blob blob;
                    try {
                        blob = new SerialBlob(bytes);
                    } catch (SerialException e) {
                        throw new GSException(e);
                    } catch (SQLException e) {
                        throw new GSException(e);
                    }
                    row.setBlob(index, blob);
                    break;
                default:
                    // Throw error
                    throw new GSException("Type is not support: " + gsType);
            }
            index++;
        }
        return row;
    }

    /**
     * Put container
     * @param containerName  the container names
     * @param fieldsMetadata Kafka fields object
     * @throws GSException
     */
    private void create(final String containerName, final FieldsMetadata fieldsMetadata)
            throws GSException {
        LOG.info("Put container with name {}", containerName);
        Map<String, SinkRecordField> allFields = fieldsMetadata.getAllFields();
        this.dialect.putContainer(containerName, allFields.values());
    }

    /**
     * Put row to database
     * @param container the container object
     * @param row       the row
     * @throws GSException
     */
    @Override
    public void putRow(Container<?, Row> container, Row row) throws GSException {
        if (container != null) {
            this.dialect.putRow(container, row);
        }
    }

    /**
     * Put multi rows to database
     * @param container the container
     * @param rows      list rows
     * @throws GSException
     */
    @Override
    public void putRows(Container<?, Row> container, List<Row> rows) throws GSException {
        if (container != null) {
            this.dialect.putRows(container, rows);
        }
    }
}
