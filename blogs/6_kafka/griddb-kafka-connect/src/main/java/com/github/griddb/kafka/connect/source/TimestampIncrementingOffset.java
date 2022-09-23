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

package com.github.griddb.kafka.connect.source;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support query data for source connector
 */
public class TimestampIncrementingOffset {
    private static final Logger LOG = LoggerFactory.getLogger(TimestampIncrementingOffset.class);
    private static final String TIMESTAMP_FIELD = "timestamp";
    private static final String TIMESTAMP_NANOS_FIELD = "timestamp_nanos";

    private final Timestamp timestampOffset;

    /**
     * Constructor
     * 
     * @param timestampOffset timestamp offset
     */
    public TimestampIncrementingOffset(Timestamp timestampOffset) {
        this.timestampOffset = timestampOffset;
    }

    /**
     * Get timestamp offset
     * 
     * @return Timestamp
     */
    public Timestamp getTimestampOffset() {
        return timestampOffset != null ? timestampOffset : new Timestamp(0L);
    }

    /**
     * Check has timestamp offset
     * 
     * @return boolean
     */
    public boolean hasTimestampOffset() {
        return timestampOffset != null;
    }

    /**
     * Convert data to map object
     * 
     * @return Map<String, Object>
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (timestampOffset != null) {
            map.put(TIMESTAMP_FIELD, timestampOffset.getTime());
            map.put(TIMESTAMP_NANOS_FIELD, (long) timestampOffset.getNanos());
        }
        return map;
    }

    /**
     * Create TimestampIncrementingOffset from map data
     * 
     * @param map
     * @return TimestampIncrementingOffset
     */
    public static TimestampIncrementingOffset fromMap(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return new TimestampIncrementingOffset(null);
        }

        Long millis = (Long) map.get(TIMESTAMP_FIELD);
        Timestamp ts = null;
        if (millis != null) {
            LOG.trace("millis is not null");
            ts = new Timestamp(millis);
            Long nanos = (Long) map.get(TIMESTAMP_NANOS_FIELD);
            if (nanos != null) {
                LOG.trace("Nanos is not null");
                ts.setNanos(nanos.intValue());
            }
        }
        return new TimestampIncrementingOffset(ts);
    }

}
