/*
    Copyright (c) 2022 TOSHIBA Digital Solutions Corporation.

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

#[repr(i32)]
#[derive(PartialEq, Eq, Debug, Clone)]
pub enum ContainerType {
    Collection = 0,
    TimeSeries = 1,
}

#[repr(i32)]
#[derive(PartialEq, Eq, Copy, Debug, Clone)]
pub enum Type {
    String,
    Bool,
    Byte,
    Short,
    Integer,
    Long,
    Float,
    Double,
    Timestamp,
    Geometry,
    Blob,
}

#[repr(i32)]
#[derive(PartialEq, Eq, Debug, Clone)]
pub enum IndexType {
    Default = -1,
    Tree = 1,
    Spatial = 4,
}

#[repr(i32)]
#[derive(PartialEq, Eq, Debug, Clone)]
pub enum TimeUnit {
    Year = 0,
    Month = 1,
    Day = 2,
    Hour = 3,
    Minute = 4,
    Second = 5,
    MilliSecond = 6,
}

#[repr(i32)]
#[derive(PartialEq, Eq, Debug, Clone)]
pub enum RowSetType {
    ContainerRows = 0,
    AggregationResult = 1,
    QueryAnalysis = 2,
}

#[repr(i32)]
#[derive(PartialEq, Eq, Copy, Debug, Clone)]
pub enum TypeOption {
    Nullable = 2,
    NotNull = 4,
}
