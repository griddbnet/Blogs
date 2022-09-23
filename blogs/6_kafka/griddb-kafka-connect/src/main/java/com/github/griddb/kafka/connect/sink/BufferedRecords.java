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

import java.util.List;

import com.toshiba.mwcloud.gs.GSException;

import org.apache.kafka.connect.sink.SinkRecord;

/**
 * The interface of write buffer
 */
public interface BufferedRecords {

    /**
     * Add record to buffer
     * @param record : the record object
     * @return list sink record object are in buffer
     * @throws GSException
     */
    List<SinkRecord> add(SinkRecord record) throws GSException;

    /**
     * Write the data from buffer to database
     * @return list record are written to database
     * @throws GSException
     */
    List<SinkRecord> flush() throws GSException;

    /**
     * Release the resource when close buffer
     * @throws GSException
     */
    void close() throws GSException;
}
