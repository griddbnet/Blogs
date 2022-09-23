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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

/**
 * The configuration class for sink connector
 */
@SuppressWarnings("PMD.LongVariable")
public class GriddbSinkConnectorConfig extends AbstractConfig {

    public static final String HOST_CONFIG = "host";
    public static final String PORT_CONFIG = "port";
    public static final String CLUSTERNAME_CONFIG = "cluster.name";
    public static final String USER_CONFIG = "user";
    public static final String PASSWORD_CONFIG = "password";
    public static final String NOTIFICATION_MEMBER_CONFIG = "notification.member";
    public static final String NOTIFICATION_PROVIDER_CONFIG = "notification.provider.url";

    public static final String USE_MULTIPUT_CONFIG = "multiput";

    public static final List<String> DEFAULT_KAFKA_PK_NAMES = Collections
            .unmodifiableList(Arrays.asList("__connect_topic", "__connect_partition", "__connect_offset"));
    private static final String WRITES_GROUP = "Writes";
    private static final String DATAMAPPING_GROUP = "Data Mapping";
    public static final String CONTAINER_NAME_FORMAT = "container.name.format";
    private static final String CONTAINER_NAME_FORMAT_DEFAULT = "${topic}";
    private static final String CONTAINER_NAME_FORMAT_DOC = "A format string for the destination container name, which may contain '${topic}' as a "
            + "placeholder for the originating topic name.\n"
            + "For example, ``kafka_${topic}`` for the topic 'orders' will map to the container name "
            + "'kafka_orders'.";
    private static final String CONTAINER_NAME_FORMAT_DISPLAY = "Container Name Format";

    public static final String BATCH_SIZE = "batch.size";
    private static final int BATCH_SIZE_DEFAULT = 3000;
    private static final String BATCH_SIZE_DOC = "Specifies how many records to attempt to batch together for insertion into the destination"
            + " container, when possible.";
    private static final String BATCH_SIZE_DISPLAY = "Batch Size";

    private static final ConfigDef.Range NON_NEGATIVE_INT_VALIDATOR = ConfigDef.Range.atLeast(0);
    public final String host;
    public final String port;
    public final String clusterName;
    public final String user;
    public final String password;
    public final String notificationMember;
    public final String notificationProviderUrl;

    public final String containerNameFormat;
    public final int batchSize;
    public final boolean useMultiPut;

    @SuppressWarnings("CPD-START")
    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .defineInternal(HOST_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(PORT_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(CLUSTERNAME_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(USER_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(PASSWORD_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(NOTIFICATION_MEMBER_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(NOTIFICATION_PROVIDER_CONFIG, Type.STRING, "", Importance.HIGH)
            .define(CONTAINER_NAME_FORMAT, ConfigDef.Type.STRING, CONTAINER_NAME_FORMAT_DEFAULT,
                    ConfigDef.Importance.MEDIUM, CONTAINER_NAME_FORMAT_DOC, DATAMAPPING_GROUP, 1, ConfigDef.Width.LONG,
                    CONTAINER_NAME_FORMAT_DISPLAY)
            .define(BATCH_SIZE, ConfigDef.Type.INT, BATCH_SIZE_DEFAULT, NON_NEGATIVE_INT_VALIDATOR,
                    ConfigDef.Importance.MEDIUM, BATCH_SIZE_DOC, WRITES_GROUP, 2, ConfigDef.Width.SHORT,
                    BATCH_SIZE_DISPLAY)
            .defineInternal(USE_MULTIPUT_CONFIG, Type.BOOLEAN, "true", Importance.LOW);
    @SuppressWarnings("CPD-END")

    /**
     * The constructor method
     * @param props : properties from config file
     */
    public GriddbSinkConnectorConfig(Map<?, ?> props) {
        super(CONFIG_DEF, props);

        host = getString(HOST_CONFIG).trim();
        port = getString(PORT_CONFIG).trim();
        clusterName = getString(CLUSTERNAME_CONFIG).trim();
        user = getString(USER_CONFIG).trim();
        password = getString(PASSWORD_CONFIG).trim();
        notificationMember = getString(NOTIFICATION_MEMBER_CONFIG).trim();
        notificationProviderUrl = getString(NOTIFICATION_PROVIDER_CONFIG).trim();

        containerNameFormat = getString(CONTAINER_NAME_FORMAT).trim();
        batchSize = getInt(BATCH_SIZE);
        useMultiPut = getBoolean(USE_MULTIPUT_CONFIG);
    }
}
