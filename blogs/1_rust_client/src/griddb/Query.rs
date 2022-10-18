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

use crate::griddb::Const::*;
use crate::griddb::RowSet::*;
use crate::griddb::Type::*;

use std::collections::HashMap;
use std::ptr;

// Class Query
pub struct Query {
    _ptr: *mut ffi::GSQuery,
    _row: *mut ffi::GSRow,
    _type_list: Vec<Type>,
}
impl Query {
    const LIMIT_KEY: &'static str = "limit";
    const PARTIAL_KEY: &'static str = "partial";
    pub fn new(query: *mut ffi::GSQuery, row: *mut ffi::GSRow, type_list: Vec<Type>) -> Query {
        Query {
            _ptr: query,
            _row: row,
            _type_list: type_list,
        }
    }

    pub fn fetch(&self) -> Result<RowSet, i32> {
        let mut p_row_set: *mut ffi::GSRowSet = ptr::null_mut();
        let ret;
        unsafe {
            ret = ffi::gsFetch(self._ptr, ffi::GS_FALSE as i8, &mut p_row_set);
        }
        if ret == ffi::GS_RESULT_OK as i32 {
            Ok(RowSet::new(p_row_set, self._row, self._type_list.clone()))
        } else {
            Err(ret)
        }
    }

    pub fn get_row_set(&self) -> Result<RowSet, i32> {
        let mut p_row_set: *mut ffi::GSRowSet = ptr::null_mut();
        let ret;
        unsafe {
            ret = ffi::gsGetRowSet(self._ptr, &mut p_row_set);
        }
        if ret == ffi::GS_RESULT_OK as i32 {
            Ok(RowSet::new(p_row_set, self._row, self._type_list.clone()))
        } else {
            Err(ret)
        }
    }

    pub fn set_fetch_options(&self, hashmap: HashMap<String, i32>) -> i32 {
        let mut limit: i32 = 0;
        let mut partial: bool = true;
        for (key, value) in &hashmap {
            match key.as_ref() {
                Query::LIMIT_KEY => limit = *value,
                Query::PARTIAL_KEY => {
                    if *value != 0 {
                        partial = true;
                    } else {
                        partial = false;
                    }
                }
                _ => {
                    return ERROR_CONVERT_DATA;
                }
            }
        }

        let ret;
        let limit_ptr: *const ::std::os::raw::c_void =
            &mut limit as *mut _ as *mut ::std::os::raw::c_void;

        let partial_ptr: *const ::std::os::raw::c_void =
            &mut partial as *mut _ as *mut ::std::os::raw::c_void;
        unsafe {
            ffi::gsSetFetchOption(
                self._ptr,
                ffi::GSFetchOptionTag_GS_FETCH_LIMIT as i32,
                limit_ptr,
                ffi::GSTypeTag_GS_TYPE_INTEGER,
            );

            ret = ffi::gsSetFetchOption(
                self._ptr,
                ffi::GSFetchOptionTag_GS_FETCH_PARTIAL_EXECUTION as i32,
                partial_ptr,
                ffi::GSTypeTag_GS_TYPE_BOOL,
            );
        }
        ret
    }
}

// Destructor
impl Drop for Query {
    fn drop(&mut self) {
        unsafe {
            ffi::gsCloseQuery(&mut self._ptr);
        }
    }
}
