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

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import com.github.griddb.kafka.connect.util.ColumnId;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

/**
 * Support query data
 */
public class TimestampIncrementingCriteria {
    private final List<ColumnId> timestampColumns;

    public TimestampIncrementingCriteria(List<ColumnId> timestampColumns) {
        this.timestampColumns = timestampColumns != null ? timestampColumns : Collections.<ColumnId>emptyList();
    }

    /**
     * Extract the offset values from the row.
     *
     * @param schema         the record's schema; never null
     * @param record         the record's struct; never null
     * @param previousOffset a previous timestamp offset if the table has timestamp
     *                       columns
     * @return the timestamp for this row; may not be null
     */
    public TimestampIncrementingOffset extractValues(Schema schema, Struct record,
            TimestampIncrementingOffset previousOffset) {
        Timestamp extractedTimestamp = null;
        if (hasTimestampColumns()) {
            extractedTimestamp = extractOffsetTimestamp(record);
            boolean hasOffset = previousOffset.getTimestampOffset() != null
                    && previousOffset.getTimestampOffset().compareTo(extractedTimestamp) <= 0;
            assert previousOffset == null || hasOffset;
        }
        return new TimestampIncrementingOffset(extractedTimestamp);
    }

    /**
     * Check config has timestamp columns
     */
    private boolean hasTimestampColumns() {
        return !timestampColumns.isEmpty();
    }

    /**
     * Extract the timestamp from the row.
     *
     * @param schema the record's schema; never null
     * @param record the record's struct; never null
     * @return the timestamp for this row; may not be null
     */
    private Timestamp extractOffsetTimestamp(Struct record) {
        for (ColumnId timestampColumn : timestampColumns) {
            Timestamp ts = (Timestamp) record.get(timestampColumn.getName());
            if (ts != null) {
                return ts;
            }
        }
        return null;
    }

}
