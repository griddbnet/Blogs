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

import org.apache.kafka.connect.data.Schema;

/**
 * SinkRecordField : Support mapping between field name and Kafka schema
 */
public class SinkRecordField {

    private final Schema schema;
    private final String name;

    /**
     * Constructor
     * @param schema Field schema
     * @param name Field name
     */
    public SinkRecordField(Schema schema, String name) {
        this.schema = schema;
        this.name = name;
    }

    /**
     * Get schema name
     * @return String
     */
    public String schemaName() {
        return schema.name();
    }


    /**
     * get schema type
     * @return Schema.Type
     */
    public Schema.Type schemaType() {
        return schema.type();
    }

    /**
     * Get name
     * @return String
     */
    public String name() {
        return name;
    }

}
