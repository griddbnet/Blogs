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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Support to get Kafka event offset
 */
@SuppressWarnings({ "PMD.LongVariable", "PMD.ClassNamingConventions" })
public class OffsetProtocols {

    private static final String CONTAINER_NAME_KEY = "container";

    private static final String OFFSET_PROTOCOL_VERSION_KEY = "protocol";
    private static final String PROTOCOL_VERSION_ONE = "1";

    /**
     *  Provides the partition map for V1 protocol.
     */
    public static Map<String, String> sourcePartitionForProtocolV1(String container) {
        Map<String, String> partitionForV1 = new HashMap<>();
        partitionForV1.put(CONTAINER_NAME_KEY, container);
        partitionForV1.put(OFFSET_PROTOCOL_VERSION_KEY, PROTOCOL_VERSION_ONE);
        return partitionForV1;
    }

    /**
     * Provides the partition map for V0 protocol. The table name included is
     * unqualified and there is no explicit protocol key.
     *
     * @param tableId the tableId that requires partition keys
     * @return the partition map for V0 protocol
     */
    public static Map<String, String> sourcePartitionForProtocolV0(String container) {
        return Collections.singletonMap(CONTAINER_NAME_KEY, container);
    }
}
