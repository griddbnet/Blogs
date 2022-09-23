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

import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

/**
 * Griddb source connector config
 */
@SuppressWarnings("PMD.LongVariable")
public class GriddbSourceConnectorConfig extends AbstractConfig {

    public static final String HOST_CONFIG = "host";
    public static final String PORT_CONFIG = "port";
    public static final String CLUSTERNAME_CONFIG = "cluster.name";
    public static final String USER_CONFIG = "user";
    public static final String PASSWORD_CONFIG = "password";
    public static final String NOTIFICATION_MEMBER_CONFIG = "notification.member";
    public static final String NOTIFICATION_PROVIDER_CONFIG = "notification.provider.url";
    public static final String POLLING_INTERVAL_CONFIG = "poll.interval.ms";
    private static final ConfigDef.Range POSITIVE_INT_VALIDATOR = ConfigDef.Range.atLeast(1);
    private static final int POLLING_INTERVAL_DEFAULT = 5000;
    private static final String POLL_INTERVAL_DOC = "Frequency in ms to poll for new data in each table.";

    public static final String DATABASE_GROUP = "Database";
    public static final String MODE_GROUP = "Mode";
    public static final String CONNECTOR_GROUP = "Connector";

    public static final String CONTAINERS_CONFIG = "containers";
    private static final String CONTAINERS_DOC = "List of containers for GriddbSourceTask to watch for changes.";

    public static final String MODE_CONFIG = "mode";
    private static final String MODE_DOC = "The mode for updating a container each time it is polled. Options include:\n"
            + "  * bulk: perform a bulk load of the entire container each time it is polled\n"
            + "  * incrementing: use a strictly incrementing column on each container to "
            + "detect only new rows. Note that this will not detect modifications or " + "deletions of existing rows.\n"
            + "  * timestamp: use a timestamp (or timestamp-like) column to detect new and modified "
            + "rows. This assumes the column is updated with each write, and that values are "
            + "monotonically incrementing, but not necessarily unique.\n"
            + "  * timestamp+incrementing: use two columns, a timestamp column that detects new and "
            + "modified rows and a strictly incrementing column which provides a globally unique ID for "
            + "updates so each row can be assigned a unique stream offset.";
    private static final String MODE_DISPLAY = "Container Loading Mode";

    public static final String MODE_UNSPECIFIED = "";
    public static final String MODE_BULK = "bulk";
    public static final String MODE_TIMESTAMP = "timestamp";
    public static final String MODE_INCREMENTING = "incrementing";
    public static final String MODE_TIMESTAMP_INCREMENTING = "timestamp+incrementing";

    public static final String TIMESTAMP_COLUMN_NAME_CONFIG = "timestamp.column.name";

    private static final String TIMESTAMP_COLUMN_NAME_DOC = "Comma separated list of one or more timestamp columns to detect new or modified rows using "
            + "the COALESCE SQL function. Rows whose first non-null timestamp value is greater than the "
            + "largest previous timestamp value seen will be discovered with each poll. At least one "
            + "column should not be nullable.";
    public static final String TIMESTAMP_COLUMN_NAME_DEFAULT = "";
    private static final String TIMESTAMP_COLUMN_NAME_DISPLAY = "Timestamp Column Name";

    public static final String TOPIC_PREFIX_CONFIG = "topic.prefix";
    private static final String TOPIC_PREFIX_DOC = "Prefix to prepend to container names to generate the name of the Kafka topic to publish data "
            + "to, or in the case of a custom query, the full name of the topic to publish to.";

    public final String host;
    public final String port;
    public final String clusterName;
    public final String user;
    public final String password;
    public final String notificationMember;
    public final String notificationProviderUrl;
    public final int pollingInterval;
    public final List<String> containers;

    @SuppressWarnings("CPD-START")
    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .defineInternal(HOST_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(PORT_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(CLUSTERNAME_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(USER_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(PASSWORD_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(NOTIFICATION_MEMBER_CONFIG, Type.STRING, "", Importance.HIGH)
            .defineInternal(NOTIFICATION_PROVIDER_CONFIG, Type.STRING, "", Importance.HIGH)
            .define(POLLING_INTERVAL_CONFIG, Type.INT, POLLING_INTERVAL_DEFAULT, POSITIVE_INT_VALIDATOR, Importance.HIGH, POLL_INTERVAL_DOC)
            .define(CONTAINERS_CONFIG, Type.LIST, Importance.HIGH, CONTAINERS_DOC)
            .define(MODE_CONFIG, Type.STRING, MODE_UNSPECIFIED,
                    ConfigDef.ValidString.in(MODE_UNSPECIFIED, MODE_BULK, MODE_TIMESTAMP), Importance.HIGH, MODE_DOC,
                    MODE_GROUP, 0, Width.MEDIUM, MODE_DISPLAY)
            .defineInternal(TOPIC_PREFIX_CONFIG, Type.STRING, TOPIC_PREFIX_DOC, Importance.HIGH)
            .define(TIMESTAMP_COLUMN_NAME_CONFIG, Type.LIST, TIMESTAMP_COLUMN_NAME_DEFAULT, Importance.MEDIUM,
                    TIMESTAMP_COLUMN_NAME_DOC, MODE_GROUP, 2, Width.MEDIUM, TIMESTAMP_COLUMN_NAME_DISPLAY);
    @SuppressWarnings("CPD-END")

    /**
     * The constructor method
     * @param props : config parameters
     */
    public GriddbSourceConnectorConfig(Map<String, ?> props) {
        super(CONFIG_DEF, props);

        host = getString(HOST_CONFIG).trim();
        port = getString(PORT_CONFIG).trim();
        clusterName = getString(CLUSTERNAME_CONFIG).trim();
        user = getString(USER_CONFIG).trim();
        password = getString(PASSWORD_CONFIG).trim();
        notificationMember = getString(NOTIFICATION_MEMBER_CONFIG).trim();
        notificationProviderUrl = getString(NOTIFICATION_PROVIDER_CONFIG).trim();
        pollingInterval = getInt(POLLING_INTERVAL_CONFIG);

        containers = getList(CONTAINERS_CONFIG);
    }
}