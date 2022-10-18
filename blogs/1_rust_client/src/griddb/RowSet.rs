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

use crate::griddb::AggregationResult::*;
use crate::griddb::Const::*;
use crate::griddb::Type::*;
use crate::griddb::Util::*;
use crate::griddb::Value::*;
use std::ptr;

// Class rowset
pub struct RowSet {
    _ptr: *mut ffi::GSRowSet,
    _row: *mut ffi::GSRow,
    pub rowset_type: RowSetType,
    pub size: i32,
    _type_list: Vec<Type>,
}
#[allow(clippy::not_unsafe_ptr_arg_deref)]
impl RowSet {
    pub fn new(row_set: *mut ffi::GSRowSet, row: *mut ffi::GSRow, type_list: Vec<Type>) -> RowSet {
        let size: i32;
        let gs_type;
        let rowset_type: RowSetType;
        unsafe {
            size = ffi::gsGetRowSetSize(row_set);
            gs_type = ffi::gsGetRowSetType(row_set);
        }
        match gs_type as u32 {
            ffi::GSRowSetTypeTag_GS_ROW_SET_CONTAINER_ROWS => {
                rowset_type = RowSetType::ContainerRows;
            }
            ffi::GSRowSetTypeTag_GS_ROW_SET_AGGREGATION_RESULT => {
                rowset_type = RowSetType::AggregationResult;
            }
            ffi::GSRowSetTypeTag_GS_ROW_SET_QUERY_ANALYSIS => {
                rowset_type = RowSetType::QueryAnalysis;
            }
            _ => {
                panic!("Cannot convert value in RowSet")
            }
        };
        RowSet {
            _ptr: row_set,
            _row: row,
            rowset_type,
            size,
            _type_list: type_list,
        }
    }
    pub fn has_next(&self) -> bool {
        let result;
        unsafe {
            result = ffi::gsHasNextRow(self._ptr);
        }
        result == ffi::GS_TRUE as i8
    }
    pub fn next(&self) -> Result<Vec<Value>, i32> {
        let ret;
        let type_rs = self.get_row_set_type();
        match type_rs as u32 {
            ffi::GSRowSetTypeTag_GS_ROW_SET_CONTAINER_ROWS => {
                unsafe {
                    ret = ffi::gsGetNextRow(self._ptr, self._row as *mut std::ffi::c_void);
                }
                if ret == ffi::GS_RESULT_OK as i32 {
                    let mut vec = Vec::new();
                    Util::get_row_data(self._row, &mut vec, &self._type_list);
                    Ok(vec)
                } else {
                    Err(ret)
                }
            }
            _ => Err(ERROR_CONVERT_DATA),
        }
    }
    pub fn next_aggregation(&self) -> Result<AggregationResult, i32> {
        let ret;
        let type_rs = self.get_row_set_type();
        let mut aff_result: *mut ffi::GSAggregationResult = ptr::null_mut();
        match type_rs as u32 {
            ffi::GSRowSetTypeTag_GS_ROW_SET_AGGREGATION_RESULT => {
                self.has_next();
                unsafe {
                    ret = ffi::gsGetNextAggregation(self._ptr, &mut aff_result);
                }
                if ret == ffi::GS_RESULT_OK as i32 {
                    Ok(AggregationResult::new(aff_result))
                } else {
                    Err(ret)
                }
            }
            _ => Err(ERROR_CONVERT_DATA),
        }
    }
    fn get_row_set_type(&self) -> i32 {
        let result;
        unsafe {
            result = ffi::gsGetRowSetType(self._ptr);
        }
        result
    }
}

// Destructor
impl Drop for RowSet {
    fn drop(&mut self) {
        unsafe {
            ffi::gsCloseRowSet(&mut self._ptr);
        }
    }
}
