/*
    Copyright (c) 2020 TOSHIBA Digital Solutions Corporation.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

var griddb = require('./griddb');

// Export enum values
const griddbconst = {
    ContainerType: {
        COLLECTION: 0,
        TIME_SERIES: 1
    },
    IndexType: {
        DEFAULT: -1,
        TREE: 1 << 0,
        HASH: 1 << 1,
        SPATIAL: 1 << 2
    },
    RowSetType: {
        CONTAINER_ROWS: 0,
        AGGREGATION_RESULT: 1,
        QUERY_ANALYSIS: 2
    },
    FetchOption: {
        LIMIT: 0
    },
    TimeUnit: {
        YEAR: 0,
        MONTH: 1,
        DAY: 2,
        HOUR: 3,
        MINUTE: 4,
        SECOND: 5,
        MILLISECOND: 6
    },
    Type: {
        STRING: 0,
        BOOL: 1,
        BYTE: 2,
        SHORT: 3,
        INTEGER: 4,
        LONG: 5,
        FLOAT: 6,
        DOUBLE: 7,
        TIMESTAMP: 8,
        GEOMETRY: 9,
        BLOB: 10,
        STRING_ARRAY: 11,
        BOOL_ARRAY: 12,
        BYTE_ARRAY: 13,
        SHORT_ARRAY: 14,
        INTEGER_ARRAY: 15,
        LONG_ARRAY: 16,
        FLOAT_ARRAY: 17,
        DOUBLE_ARRAY: 18,
        TIMESTAMP_ARRAY: 19,
        NULL: -1
    },
    TypeOption: {
        NULLABLE: 1 << 1,
        NOT_NULL: 1 << 2
    }
};

for (var key in griddbconst) {
    griddb[key] = griddbconst[key];
}

module.exports = griddb;
