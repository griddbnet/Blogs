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

package com.github.griddb.kafka.connect.source;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.griddb.kafka.connect.dialect.DbDialect;
import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Row;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support mapping data between Griddb container and Kafka topic
 */
public final class SchemaMapping {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaMapping.class);

    private final Schema schema;

    private ContainerInfo containerInfo;

    private List<Field> fields = new ArrayList<>();

    /**
     * Create the SchemaMapping object
     * 
     * @param containerName the containerName
     * @param containerInfo the containerInfo
     * @param dialect       the database dialect
     * @return SchemaMapping
     * @throws GSException
     */
    public static SchemaMapping create(String containerName, ContainerInfo containerInfo, DbDialect dialect)
            throws GSException {
        SchemaBuilder builder = SchemaBuilder.struct().name(containerName);
        List<Field> fields = new ArrayList<>();

        for (int i = 0; i < containerInfo.getColumnCount(); i++) {
            ColumnInfo colInfo = containerInfo.getColumnInfo(i);
            String fieldName = colInfo.getName();
            if (fieldName == null) {
                continue;
            }
            switch (colInfo.getType()) {
                case STRING:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_STRING_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.STRING_SCHEMA);
                    }
                    break;
                case BOOL:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_BOOLEAN_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.BOOLEAN_SCHEMA);
                    }
                    break;
                case BYTE:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_INT8_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.INT8_SCHEMA);
                    }
                    break;
                case SHORT:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_INT16_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.INT16_SCHEMA);
                    }
                    break;
                case INTEGER:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_INT32_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.INT32_SCHEMA);
                    }
                    break;
                case LONG:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_INT64_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.INT64_SCHEMA);
                    }
                    break;
                case FLOAT:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_FLOAT32_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.FLOAT32_SCHEMA);
                    }
                    break;
                case DOUBLE:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_FLOAT64_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.FLOAT64_SCHEMA);
                    }
                    break;
                case TIMESTAMP:
                    SchemaBuilder tsSchemaBuilder = org.apache.kafka.connect.data.Timestamp.builder();
                    if (colInfo.getNullable()) {
                        tsSchemaBuilder.optional();
                    }
                    builder.field(fieldName, tsSchemaBuilder.build());
                    break;
                case BLOB:
                    if (colInfo.getNullable()) {
                        builder.field(fieldName, Schema.OPTIONAL_BYTES_SCHEMA);
                    } else {
                        builder.field(fieldName, Schema.BYTES_SCHEMA);
                    }
                    break;
                default:
                    throw new GSException("Type is not support");
            }
            Field field = builder.field(fieldName);
            fields.add(field);
        }
        Schema schema = builder.build();
        return new SchemaMapping(schema, containerInfo, fields);
    }

    /**
     * Private constructor
     * 
     * @param schema        the Kafka topic schema
     * @param containerInfo the ContainerInfo object
     * @param fields        the Kafka topic fields
     */
    private SchemaMapping(Schema schema, ContainerInfo containerInfo, List<Field> fields) {
        this.schema = schema;
        this.containerInfo = containerInfo;
        this.fields = fields;
    }

    /**
     * Set data for Kafka event
     * 
     * @param struct the output Kafka event
     * @param row    the Griddb row data
     * @throws GSException
     */
    public void setStructRecordField(Struct struct, Row row) throws GSException {
        for (int i = 0; i < containerInfo.getColumnCount(); i++) {
            Field field = this.fields.get(i);
            ColumnInfo columnInfo = containerInfo.getColumnInfo(i);
            Object value;
            switch (columnInfo.getType()) {
                case STRING:
                    value = row.getString(i);
                    break;
                case BOOL:
                    value = row.getBool(i);
                    break;
                case BYTE:
                    value = row.getByte(i);
                    break;
                case SHORT:
                    value = row.getShort(i);
                    break;
                case INTEGER:
                    value = row.getInteger(i);
                    break;
                case LONG:
                    value = row.getLong(i);
                    break;
                case FLOAT:
                    value = row.getFloat(i);
                    break;
                case DOUBLE:
                    value = row.getDouble(i);
                    break;
                case TIMESTAMP:
                    value = new java.sql.Timestamp(row.getTimestamp(i).getTime());
                    break;
                case BLOB:
                    Blob blob = row.getBlob(i);
                    if (blob != null) {
                        try {
                            value = blob.getBytes(1, (int) blob.length());
                        } catch (SQLException e) {
                            throw new GSException("Error when convert Blob value", e);
                        }
                    } else {
                        value = null;
                    }
                    break;
                default:
                    throw new GSException("Type is not support in schema mapping");
            }

            LOG.debug("Field number {} with value {}", i, value);
            struct.put(field, value);
        }
    }

    /**
     * Get Kafka topic schema
     * 
     * @return
     */
    public Schema getSchema() {
        return schema;
    }
}
