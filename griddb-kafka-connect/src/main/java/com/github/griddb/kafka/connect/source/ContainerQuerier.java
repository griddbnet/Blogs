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

import com.github.griddb.kafka.connect.dialect.DbDialect;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;

import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContainerQuerier: the container querier to get all data from Griddb container
 */
@SuppressWarnings("PMD.AbstractNaming")
abstract class ContainerQuerier implements Comparable<ContainerQuerier> {

    private static final Logger LOG = LoggerFactory.getLogger(ContainerQuerier.class);

    protected long lastUpdate;
    protected final String containerName;
    protected final DbDialect dialect;
    protected final String topicPrefix;
    protected RowSet<Row> rowset;
    private String loggedQueryString;

    protected ContainerInfo containerInfo;
    protected SchemaMapping schemaMapping;

    /**
     * The constructor method
     * 
     * @param dialect       the database dialect
     * @param containerName the container name
     * @param topicPrefix   the Kafka topic prefix
     */
    public ContainerQuerier(DbDialect dialect, String containerName, String topicPrefix) {
        this.dialect = dialect;
        this.containerName = containerName;
        this.topicPrefix = topicPrefix;
        this.lastUpdate = 0;
    }

    /**
     * Support sort CotainerQuerier objects
     */
    @Override
    public int compareTo(ContainerQuerier other) {
        if (this.lastUpdate < other.lastUpdate) {
            return -1;
        } else if (this.lastUpdate > other.lastUpdate) {
            return 1;
        }
        return this.containerName.compareTo(other.containerName);
    }

    /**
     * Get the last time when the querier is updated
     * 
     * @return long
     */
    public long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Create Kafka source event base on row data
     * 
     * @return SourceRecord
     * @throws GSException
     */
    public abstract SourceRecord extractRecord() throws GSException;

    /**
     * Reset data of querier
     * 
     * @param now
     */
    public void reset(long now) {
        this.closeRowSet();
        schemaMapping = null;
        lastUpdate = now;
    }

    /**
     * Create TQL statement to get data
     */
    public abstract String createQueryStatement();

    /**
     * Get RowSet object
     * 
     * @return RowSet
     * @throws GSException
     */
    public RowSet<Row> getRowSet() throws GSException {
        String tql = this.createQueryStatement();
        return this.dialect.getRowSet(this.containerName, tql);
    }

    /**
     * Check the querier has next data
     * 
     * @return
     * @throws GSException
     */
    public boolean hasNext() throws GSException {
        if (rowset != null) {
            return rowset.hasNext();
        }
        return false;
    }

    /**
     * Prepare to query data
     * 
     * @throws GSException
     */
    public void startQuery() throws GSException {
        if (rowset == null) {
            rowset = getRowSet();
            if (this.containerInfo == null) {
                this.containerInfo = this.dialect.getContainerInfo(this.containerName);
            }
            schemaMapping = SchemaMapping.create(this.containerName, this.containerInfo, dialect);
        }
    }

    /**
     * Close RowSet object
     */
    private void closeRowSet() {
        if (rowset != null) {
            try {
                rowset.close();
            } catch (GSException e) {
                // Print stack trace for supporting debug
                e.printStackTrace();
            }
        }
        rowset = null;
    }

    /**
     * Support check TQL string in debug mode
     * 
     * @param tql
     */
    protected void recordQuery(String tql) {
        if (tql != null && !tql.equals(loggedQueryString)) {
            // For usability, log the statement at DEBUG level only when it changes
            LOG.debug("Begin using DEBUG query: {}", tql);
            loggedQueryString = tql;
        }
    }

    /**
     * Get row data
     */
    protected Row getRow() throws GSException {
        return this.rowset.next();
    }

    /**
     * Check the querier is getting data
     * 
     * @return
     */
    public boolean querying() {
        return this.rowset != null;
    }

}
