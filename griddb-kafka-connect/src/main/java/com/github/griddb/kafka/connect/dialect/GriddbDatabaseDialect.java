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

package com.github.griddb.kafka.connect.dialect;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.github.griddb.kafka.connect.sink.GriddbSinkConnectorConfig;
import com.github.griddb.kafka.connect.sink.SinkRecordField;
import com.github.griddb.kafka.connect.source.TimestampIncrementingCriteria;
import com.github.griddb.kafka.connect.util.ColumnId;
import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.ContainerType;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.TimestampUtils;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema.Type;
import org.apache.kafka.connect.data.Time;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Dialect class to working with Griddb
 */
@SuppressWarnings("PMD.LongVariable")
public class GriddbDatabaseDialect implements DbDialect {

    private AbstractConfig config;
    private GridStore store;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, ContainerInfo> cacheContainerInfo = new HashMap<>();

    /**
     * Contructor method
     * 
     * @param configParam the configuration object
     */
    public GriddbDatabaseDialect(AbstractConfig configParam) {
        this.config = configParam;
        String host = config.getString(GriddbSinkConnectorConfig.HOST_CONFIG);
        String port = config.getString(GriddbSinkConnectorConfig.PORT_CONFIG);
        String clusterName = config.getString(GriddbSinkConnectorConfig.CLUSTERNAME_CONFIG);
        String username = config.getString(GriddbSinkConnectorConfig.USER_CONFIG);
        String password = config.getString(GriddbSinkConnectorConfig.PASSWORD_CONFIG);
        String notificationMember = config.getString(GriddbSinkConnectorConfig.NOTIFICATION_MEMBER_CONFIG);
        String notificationProviderUrl = config.getString(GriddbSinkConnectorConfig.NOTIFICATION_PROVIDER_CONFIG);

        Properties gsprops = new Properties();
        boolean isMulticast = this.isMulticast(host);
        if (host.length() > 0 && isMulticast) {
            gsprops.setProperty("notificationAddress", host);
        } else if (host.length() > 0 && !isMulticast) {
            gsprops.setProperty("host", host);
        }
        if (port.length() > 0 && isMulticast) {
            gsprops.setProperty("notificationPort", port);
        } else if (port.length() > 0 && !isMulticast) {
            gsprops.setProperty("port", port);
        }
        if (clusterName.length() > 0) {
            gsprops.setProperty("clusterName", clusterName);
        }
        if (username.length() > 0) {
            gsprops.setProperty("user", username);
        }
        if (password.length() > 0) {
            gsprops.setProperty("password", password);
        }
        if (notificationMember.length() > 0) {
            gsprops.setProperty("notificationMember", notificationMember);
        }
        if (notificationProviderUrl.length() > 0) {
            gsprops.setProperty("notificationProvider", notificationProviderUrl);
        }

        try {
            store = GridStoreFactory.getInstance().getGridStore(gsprops);
        } catch (Exception ex) {
            log.info("Error when get gridstore.");
            throw new ConnectException("Failed to get GS instance", ex);
        }
        log.info("Get GSStore successfully.");
    }

    /**
     * Get container info object
     * 
     * @param containerName the container name
     * @return ContainerInfo object
     * @throws GSException
     */
    @Override
    public ContainerInfo getContainerInfo(String containerName) throws GSException {
        log.info("Get Container info of container {}", containerName);
        ContainerInfo containerInfo = cacheContainerInfo.get(containerName);
        if (containerInfo == null) {
            containerInfo = store.getContainerInfo(containerName);
            if (containerInfo != null) {
                cacheContainerInfo.put(containerName, containerInfo);
            }
        }

        return containerInfo;
    }

    /**
     * Create container
     * 
     * @param containerName the container name
     * @param fields        the fields information get from Kafka
     * @return Container
     * @throws GSException
     */
    @Override
    public Container<?, Row> putContainer(String containerName, Collection<SinkRecordField> fields) throws GSException {

        ContainerType type = ContainerType.COLLECTION;
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        boolean rowKeyAssigned = true;
        boolean modifiable = true;
        for (SinkRecordField field : fields) {
            String columnName = field.name();
            Type fieldType = field.schemaType();
            GSType gsType = this.getGsType(field);
            columnInfoList.add(new ColumnInfo(columnName, gsType));
            log.info("Column name {}, Kafka type : {}, gsType :{}", columnName, fieldType, gsType);
        }
        ContainerInfo containerInfo = new ContainerInfo(containerName, type, columnInfoList, rowKeyAssigned);
        return this.store.putContainer(containerName, containerInfo, modifiable);
    }

    /**
     * Get Griddb field type
     * 
     * @param field Kafka field object
     * @return Griddb type field
     */
    @Override
    public GSType getGsType(SinkRecordField field) {
        if (field.schemaName() != null) {
            switch (field.schemaName()) {
                case Decimal.LOGICAL_NAME:
                case Date.LOGICAL_NAME:
                case Time.LOGICAL_NAME:
                case Timestamp.LOGICAL_NAME:
                    return GSType.TIMESTAMP;
                default:
                    // pass through to normal types
            }
        }

        switch (field.schemaType()) {
            case INT8:
                return GSType.BYTE;
            case INT16:
                return GSType.SHORT;

            case INT32:
                return GSType.INTEGER;

            case INT64:
                return GSType.LONG;

            case FLOAT32:
                return GSType.FLOAT;

            case FLOAT64:
                return GSType.DOUBLE;

            case BOOLEAN:
                return GSType.BOOL;

            case STRING:
                return GSType.STRING;

            case BYTES:
                return GSType.BLOB;

            default:
                throw new ConnectException("Unsupported type for column value: " + field.schemaType());
        }
    }

    /**
     * Get container
     * 
     * @param containerName the container name
     * @return Container object
     * @throws GSException
     */
    @Override
    public Container<?, Row> getContainer(String containerName) throws GSException {
        return this.store.getContainer(containerName);
    }

    /**
     * Check the IP address is multicast address
     * 
     * @param addressStr the IP address
     * @return boolean : true if the ip address is multicast
     */
    private boolean isMulticast(String addressStr) {
        InetAddress multicastAddress;
        try {
            multicastAddress = InetAddress.getByName(addressStr);
        } catch (UnknownHostException e) {
            return false;
        }
        return multicastAddress.isMulticastAddress();
    }

    /**
     * Create Griddb row
     * 
     * @param container the Griddb container object
     * @param row       the Griddb row object
     * @throws GSException
     */
    @Override
    public void putRow(Container<?, Row> container, Row row) throws GSException {
        container.put(row);
    }

    /**
     * Crete Griddb rows
     * 
     * @param container the Griddb container object
     * @param rows      list rows
     * @throws GSException
     */
    @Override
    public void putRows(Container<?, Row> container, List<Row> rows) throws GSException {
        container.put(rows);
    }

    /**
     * Get RowSet object from tql
     * 
     * @param containerName the container name
     * @param tql           the TQL string
     * @return RowSet object
     * @throws GSException
     */
    @Override
    public RowSet<Row> getRowSet(String containerName, String tql) throws GSException {
        Container<?, Row> container = this.getContainer(containerName);
        Query<Row> query = container.query(tql);
        return query.fetch();
    }

    /**
     * Close the database dialect
     * 
     * @throws GSException
     */
    @Override
    public void close() throws GSException {
        if (this.store != null) {
            this.store.close();
        }
    }

    /**
     * Get the current time on database
     * 
     * @return
     */
    @Override
    public java.util.Date currentTimeOnDB() {
        return TimestampUtils.current();
    }

    /**
     * Get the TimestampIncrementingCriteria object for source connector
     * 
     * @param timestampColumns : list of timestamp column
     * @return
     */
    @Override
    public TimestampIncrementingCriteria criteriaFor(List<ColumnId> timestampColumns) {
        return new TimestampIncrementingCriteria(timestampColumns);
    }
}
