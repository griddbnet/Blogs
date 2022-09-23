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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;

/**
 * Support mapping data between Kafka event schema and Griddb container schema
 */
public class FieldsMetadata {
    private final Set<String> fieldNames;
    private final Map<String, SinkRecordField> allFields;

    /**
     * constructor
     * 
     * @param fieldNames
     * @param allFields
     */
    public FieldsMetadata(Set<String> fieldNames, Map<String, SinkRecordField> allFields) {
        boolean fieldCountsMatch = (fieldNames.size() == allFields.size()) ? true : false;
        boolean allFieldsContained = allFields.keySet().containsAll(fieldNames);
        if (!fieldCountsMatch || !allFieldsContained) {
            throw new IllegalArgumentException(
                    String.format("Validation fail -- fieldNames:%s allFields:%s", fieldNames, allFields));
        }

        this.fieldNames = fieldNames;
        this.allFields = allFields;
    }

    /**
     * Get FieldsMetadata from specific container name
     * 
     * @param containerName container's name
     * @param keySchema     Kafka key shema
     * @param valueSchema   Kafka schema
     * @return FieldsMetadata
     */
    public static FieldsMetadata extract(final String containerName, final Schema keySchema, final Schema valueSchema) {
        if (valueSchema != null && valueSchema.type() != Schema.Type.STRUCT) {
            throw new ConnectException("Value schema must be of type Struct");
        }

        final Map<String, SinkRecordField> allFields = new HashMap<>();

        final Set<String> fieldNames = new LinkedHashSet<>();

        if (valueSchema != null) {
            for (Field field : valueSchema.fields()) {

                fieldNames.add(field.name());

                final Schema fieldSchema = field.schema();
                allFields.put(field.name(), new SinkRecordField(fieldSchema, field.name()));
            }
        }

        if (allFields.isEmpty()) {
            throw new ConnectException("No fields found using key and value schemas for container: " + containerName);
        }

        final Map<String, SinkRecordField> allFieldsOrdered = new LinkedHashMap<>();
        for (String fieldName : GriddbSinkConnectorConfig.DEFAULT_KAFKA_PK_NAMES) {
            if (allFields.containsKey(fieldName)) {
                allFieldsOrdered.put(fieldName, allFields.get(fieldName));
            }
        }

        if (valueSchema != null) {
            for (Field field : valueSchema.fields()) {
                String fieldName = field.name();
                if (allFields.containsKey(fieldName)) {
                    allFieldsOrdered.put(fieldName, allFields.get(fieldName));
                }
            }
        }

        if (allFieldsOrdered.size() < allFields.size()) {
            ArrayList<String> fieldKeys = new ArrayList<>(allFields.keySet());
            Collections.sort(fieldKeys);
            for (String fieldName : fieldKeys) {
                if (!allFieldsOrdered.containsKey(fieldName)) {
                    allFieldsOrdered.put(fieldName, allFields.get(fieldName));
                }
            }
        }

        return new FieldsMetadata(fieldNames, allFieldsOrdered);
    }

    /**
     * Get all fields name
     * 
     * @return Set<String>
     */
    public Set<String> getFieldNames() {
        return this.fieldNames;
    }

    /**
     * Get all fields
     * 
     * @return Map<String, SinkRecordField>
     */
    public Map<String, SinkRecordField> getAllFields() {
        return this.allFields;
    }

}
