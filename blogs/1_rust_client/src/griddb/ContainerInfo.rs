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

extern crate griddb_sys as ffi;

use crate::griddb::Type::*;
use std::ffi::CString;
use std::ptr;

pub struct ContainerInfo {
    pub name: String,
    pub container_type: ContainerType,
    pub column_count: u64,
    // public attribute follow reference
    pub column_info_list: Vec<(String, Type, TypeOption)>,
    pub row_key: bool,
    // Support put container
    _column_info_list: Vec<ffi::GSColumnInfo>,
}

impl ContainerInfo {
    pub fn ContainerInfo(
        name: &str,
        col_info: Vec<(&str, Type)>,
        container_type: ContainerType,
        row_key: bool,
    ) -> ContainerInfo {
        let mut vec_col_info: Vec<(String, Type, TypeOption)> = vec![];
        let mut gs_col_info: Vec<ffi::GSColumnInfo> = vec![];
        let mut _number_column = 0;
        for (column_name, col_type) in col_info {
            let c_string = column_name.to_string();
            let c_string_vec = CString::new(column_name).unwrap();
            let option_type: TypeOption = match vec_col_info.len() {
                0 => TypeOption::NotNull,
                _ => TypeOption::Nullable,
            };
            vec_col_info.push((c_string, col_type, option_type));
            gs_col_info.push(ffi::GSColumnInfo {
                name: c_string_vec.into_raw(),
                type_: col_type as i32,
                indexTypeFlags: ffi::GSIndexTypeFlagTag_GS_INDEX_FLAG_DEFAULT,
                options: option_type as i32,
            });
            _number_column += 1;
        }
        ContainerInfo {
            name: name.to_string(),
            container_type,
            column_count: _number_column as u64,
            column_info_list: vec_col_info,
            row_key,
            _column_info_list: gs_col_info,
        }
    }

    pub fn unwrap(&self) -> ffi::GSContainerInfo {
        ffi::GSContainerInfo {
            name: ptr::null_mut(),
            type_: self.container_type.clone() as i32,
            columnCount: self.column_count,
            columnInfoList: self._column_info_list.as_ptr(),
            rowKeyAssigned: self.row_key as i8,
            // Default value
            columnOrderIgnorable: false as i8,
            timeSeriesProperties: ptr::null_mut(),
            triggerInfoCount: 0,
            triggerInfoList: ptr::null_mut(),
            dataAffinity: ptr::null_mut(),
            indexInfoCount: 0,
            indexInfoList: ptr::null_mut(),
            rowKeyColumnCount: 0,
            rowKeyColumnList: ptr::null_mut(),
        }
    }
}
