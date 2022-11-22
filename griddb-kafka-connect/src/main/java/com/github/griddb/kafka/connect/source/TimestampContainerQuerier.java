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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.griddb.kafka.connect.dialect.DbDialect;
import com.github.griddb.kafka.connect.util.ColumnId;
import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.Row;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support query with timestamp mode
 */
@SuppressWarnings("PMD.LongVariable")
public class TimestampContainerQuerier extends ContainerQuerier {

    private static final Logger LOG = LoggerFactory.getLogger(TimestampContainerQuerier.class);

    private TimestampIncrementingOffset offset;
    private TimestampIncrementingCriteria criteria;
    private Timestamp beginTimetampValue;
    private Timestamp endTimetampValue;
    private final List<String> timestampColumnNames;
    private final List<ColumnId> timestampColumns;

    private final Map<String, String> partition;

    /**
     * The constructor
     */
    public TimestampContainerQuerier(DbDialect dialect, String containerName, String topicPrefix,
            List<String> timestampColumnNames, Map<String, Object> offsetMap) throws GSException {
        super(dialect, containerName, topicPrefix);
        this.offset = TimestampIncrementingOffset.fromMap(offsetMap);

        partition = OffsetProtocols.sourcePartitionForProtocolV1(containerName);

        this.offset = TimestampIncrementingOffset.fromMap(offsetMap);
        this.timestampColumnNames = timestampColumnNames != null ? timestampColumnNames
                : Collections.<String>emptyList();
        this.timestampColumns = new ArrayList<>();
        this.containerInfo = this.dialect.getContainerInfo(this.containerName);
        ColumnInfo colInfo;
        String fieldName;
        for (String timestampColumn : this.timestampColumnNames) {
            if (timestampColumn == null || timestampColumn.isEmpty()) {
                continue;
            }
            for (int i = 0; i < this.containerInfo.getColumnCount(); i++) {
                colInfo = containerInfo.getColumnInfo(i);
                fieldName = colInfo.getName();
                if (fieldName.equals(timestampColumn) && colInfo.getType() == GSType.TIMESTAMP) {
                    timestampColumns.add(new ColumnId(containerName, timestampColumn));
                    break;
                }
            }
        }
        if (timestampColumns.size() == 0) {
            LOG.info("TimestampContainerQuerier : Container {} doesn't have any timestamp column in the setting",
                    this.containerName);
            throw new GSException("TimestampContainerQuerier doesn't have any timestamp column.");
        }
    }

    /**
     * Create TQL statement to get data
     */
    @Override
    public String createQueryStatement() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("SELECT * ");
        if (timestampColumns.size() > 0) {
            beginTimetampValue = offset.getTimestampOffset();
            java.util.Date currentDbTime = dialect.currentTimeOnDB();
            endTimetampValue = new Timestamp(currentDbTime.getTime());
            stringbuilder.append("WHERE ");
            coalesceTimestampColumns(stringbuilder);
            stringbuilder.append(" > ");
            stringbuilder.append("TO_TIMESTAMP_MS(");
            stringbuilder.append(beginTimetampValue.getTime());
            stringbuilder.append(")");
            stringbuilder.append(" AND ");
            coalesceTimestampColumns(stringbuilder);
            stringbuilder.append(" < ");
            stringbuilder.append("TO_TIMESTAMP_MS(");
            stringbuilder.append(endTimetampValue.getTime());
            stringbuilder.append(") ");
            stringbuilder.append(" ORDER BY ");
            coalesceTimestampColumns(stringbuilder);
            stringbuilder.append(" ASC");
        }

        criteria = dialect.criteriaFor(timestampColumns);
        String tql = stringbuilder.toString();
        recordQuery(tql);
        LOG.debug("TQL using in TimestampContainerQuerier : {}", tql);
        return tql;
    }

    /**
     * Create Kafka source event base on row data
     * 
     * @return SourceRecord
     * @throws GSException
     */
    @Override
    public SourceRecord extractRecord() throws GSException {
        LOG.debug("Extract record from Griddb Source Connector for container {}", this.containerName);
        Struct record = new Struct(schemaMapping.getSchema());
        Row row = this.getRow();
        schemaMapping.setStructRecordField(record, row);

        final String topic = this.topicPrefix + this.containerName;

        offset = criteria.extractValues(schemaMapping.getSchema(), record, offset);
        return new SourceRecord(partition, offset.toMap(), topic, record.schema(), record);
    }

    /**
     * Support to create the condition in TQL query string
     * 
     * @param builder
     */
    protected void coalesceTimestampColumns(StringBuilder builder) {
        if (timestampColumns.size() == 0) {
            return;
        }
        if (timestampColumns.size() == 1) {
            builder.append(timestampColumns.get(0).getName());
        } else {
            // GridDB also support keyword COALESCE
            builder.append("COALESCE(");
            for (int i = 0; i < timestampColumns.size() - 1; i++) {
                builder.append(timestampColumns.get(i).getName());
                builder.append(",");
            }
            builder.append(timestampColumns.get(timestampColumns.size() - 1).getName());

            builder.append(")");
        }
    }

}
