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

use chrono::{DateTime, Utc};
use std::ptr;
use std::time::{Duration, UNIX_EPOCH};

// Class AggregationResult
pub struct AggregationResult {
    _ptr: *mut ffi::GSAggregationResult,
}
impl AggregationResult {
    pub fn new(ptr_agg: *mut ffi::GSAggregationResult) -> AggregationResult {
        AggregationResult { _ptr: ptr_agg }
    }
    pub fn get_as_i64(&self) -> (i32, i64) {
        let result: *mut i64;
        let value_result: i64;
        let value_bool = ptr::null_mut();
        let ret: i32;
        unsafe {
            result = ffi::malloc(std::mem::size_of::<i64>() as u64) as *mut i64;
            ret = ffi::gsGetAggregationValueAsLong(self._ptr, result, value_bool);
            value_result = *result;
            ffi::free(result as *mut _);
        }
        (ret, value_result)
    }
    pub fn get_as_f64(&self) -> (i32, f64) {
        let result: *mut f64;
        let value_result: f64;
        let value_bool = ptr::null_mut();
        let ret: i32;
        unsafe {
            result = ffi::malloc(std::mem::size_of::<f64>() as u64) as *mut f64;
            ret = ffi::gsGetAggregationValueAsDouble(self._ptr, result, value_bool);
            value_result = *result;
            ffi::free(result as *mut _);
        }
        (ret, value_result)
    }
    pub fn get_as_timestamp(&self) -> (i32, DateTime<Utc>) {
        let result: *mut i64;
        let value_result: i64;
        let value_bool = ptr::null_mut();
        let ret: i32;
        unsafe {
            result = ffi::malloc(std::mem::size_of::<i64>() as u64) as *mut i64;
            ret = ffi::gsGetAggregationValueAsTimestamp(self._ptr, result, value_bool);
            value_result = *result;
            ffi::free(result as *mut _);
        }
        let timestamp = UNIX_EPOCH + Duration::from_millis(value_result as u64);
        // Return DateTime
        (ret, DateTime::<Utc>::from(timestamp))
    }
}

// Destructor
impl Drop for AggregationResult {
    fn drop(&mut self) {
        unsafe {
            ffi::gsCloseAggregationResult(&mut self._ptr);
        }
    }
}
