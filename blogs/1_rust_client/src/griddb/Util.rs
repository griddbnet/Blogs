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
use crate::griddb::Value::*;
use convert_case::{Case, Casing};
use std::ffi::CString;
use std::vec::Vec;
use std::{slice, str};

pub type GSRow = ffi::GSRowTag;

#[macro_export]
macro_rules! num_to_enum {
    ($num:expr => $enm:ident<$tpe:ty>{ $($fld:ident),+ }; $err:expr) => ({
        match $num {
            $(_ if $num == $enm::$fld as $tpe => { $enm::$fld })+
            _ => $err
        }
    });
}

pub struct Util;
impl Util {
    // Prepare properties for function getStore()
    pub fn tup_to_properties(
        properties: Vec<(&str, &str)>,
        temp: &mut Vec<CString>,
    ) -> Vec<ffi::GSPropertyEntry> {
        let mut result: Vec<ffi::GSPropertyEntry> = vec![];
        let mut length = temp.len();
        for &(key, value) in &properties {
            let camelCaseKey: String = key.to_string().to_case(Case::Camel);
            let key = CString::new(camelCaseKey)
                .unwrap_or_else(|_| panic!("Error convert {} to CString", key));
            let value = CString::new(value)
                .unwrap_or_else(|_| panic!("Error convert {} to CString", value));
            temp.push(key);
            temp.push(value);
            result.push(ffi::GSPropertyEntryTag {
                name: temp[length].as_ptr(),
                value: temp[length + 1].as_ptr(),
            });
            length += 2;
        }
        result
    }

    #[allow(clippy::not_unsafe_ptr_arg_deref)]
    pub fn get_row_data(row: *mut GSRow, vector: &mut Vec<Value>, typeList: &[Type]) -> i32 {
        for (pos, fieldType) in typeList.iter().enumerate() {
            match *fieldType {
                Type::String => {
                    let (ret, str) = get_row_field_as_str(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(str.to_string());
                    (*vector).push(value);
                }
                Type::Bool => {
                    let (ret, boolValue) = get_row_field_as_bool(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(boolValue);
                    (*vector).push(value);
                }
                Type::Byte => {
                    let (ret, rawdata) = get_row_field_as_byte(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(rawdata);
                    (*vector).push(value);
                }
                Type::Short => {
                    let (ret, rawdata) = get_row_field_as_short(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(rawdata);
                    (*vector).push(value);
                }
                Type::Integer => {
                    let (ret, rawdata) = get_row_field_as_interger(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(rawdata);
                    (*vector).push(value);
                }
                Type::Long => {
                    let (ret, longValue) = get_row_field_as_long(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(longValue);
                    (*vector).push(value);
                }
                Type::Float => {
                    let (ret, rawdata) = get_row_field_as_float(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(rawdata);
                    (*vector).push(value);
                }
                Type::Double => {
                    let (ret, rawdata) = get_row_field_as_double(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(rawdata);
                    (*vector).push(value);
                }
                Type::Timestamp => {
                    let (ret, rawdata) = get_row_field_as_timestamp(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(rawdata);
                    (*vector).push(value);
                }
                Type::Geometry => {}
                Type::Blob => {
                    let (ret, rawdata) = get_row_field_as_blob(row, pos);
                    if ret != ffi::GS_RESULT_OK as i32 {
                        return ret;
                    }
                    let value: Value = Value::new(rawdata);
                    (*vector).push(value);
                }
            }            
        }
        ffi::GS_RESULT_OK as i32
    }
}

// get field data for row
fn get_row_field_as_str(row: *mut GSRow, column: usize) -> (i32, String) {
    let void_ptr;
    unsafe {
        void_ptr = ffi::malloc(std::mem::size_of::<i8>() as u64);
    }
    let mut result: *const i8 = void_ptr as *const i8;
    let tmp_string;
    let ret: i32;
    unsafe {
        ret = ffi::gsGetRowFieldAsString(row, column as i32, &mut result);
        tmp_string = std::ffi::CStr::from_ptr(result as *const i8);
        ffi::free(void_ptr as *mut _);
    }
    (ret, tmp_string.to_string_lossy().into_owned())
}

fn get_row_field_as_bool(row: *mut GSRow, column: usize) -> (i32, bool) {
    let bool_v: *mut ffi::GSBool;
    let value_result;
    let ret: i32;
    unsafe {
        bool_v = ffi::malloc(std::mem::size_of::<ffi::GSBool>() as u64) as *mut ffi::GSBool;
        ret = ffi::gsGetRowFieldAsBool(row, column as i32, bool_v);
        value_result = *bool_v;
        ffi::free(bool_v as *mut _);
    }
    (ret, value_result != ffi::GS_RESULT_OK.try_into().unwrap())
}

/// # Safety
///
/// get row data field as byte
fn get_row_field_as_byte(row: *mut GSRow, column: usize) -> (i32, i8) {
    let result: *mut i8;
    let ret: i32;
    let value_result: i8;
    unsafe {
        result = ffi::malloc(std::mem::size_of::<i8>() as u64) as *mut i8;
        ret = ffi::gsGetRowFieldAsByte(row, column as i32, result);
        value_result = *result;
        ffi::free(result as *mut _);
    }
    (ret, value_result)
}
fn get_row_field_as_short(row: *mut GSRow, column: usize) -> (i32, i16) {
    let result: *mut i16;
    let value_result: i16;
    let ret: i32;
    unsafe {
        result = ffi::malloc(std::mem::size_of::<i16>() as u64) as *mut i16;
        ret = ffi::gsGetRowFieldAsShort(row, column as i32, result);
        value_result = *result;
        ffi::free(result as *mut _);
    }
    (ret, value_result)
}
fn get_row_field_as_interger(row: *mut GSRow, column: usize) -> (i32, i32) {
    let result: *mut i32;
    let value_result: i32;
    let ret: i32;
    unsafe {
        result = ffi::malloc(std::mem::size_of::<i32>() as u64) as *mut i32;
        ret = ffi::gsGetRowFieldAsInteger(row, column as i32, result);
        value_result = *result;
        ffi::free(result as *mut _);
    }
    (ret, value_result)
}

fn get_row_field_as_long(row: *mut GSRow, column: usize) -> (i32, i64) {
    let result: *mut i64;
    let value_result: i64;
    let ret: i32;
    unsafe {
        result = ffi::malloc(std::mem::size_of::<i64>() as u64) as *mut i64;
        ret = ffi::gsGetRowFieldAsLong(row, column as i32, result);
        value_result = *result;
        ffi::free(result as *mut _);
    }
    (ret, value_result)
}

fn get_row_field_as_float(row: *mut GSRow, column: usize) -> (i32, f32) {
    let result: *mut f32;
    let value_result: f32;
    let ret: i32;
    unsafe {
        result = ffi::malloc(std::mem::size_of::<f32>() as u64) as *mut f32;
        ret = ffi::gsGetRowFieldAsFloat(row, column as i32, result);
        value_result = *result;
        ffi::free(result as *mut _);
    }
    (ret, value_result)
}

fn get_row_field_as_double(row: *mut GSRow, column: usize) -> (i32, f64) {
    let result: *mut f64;
    let value_result: f64;
    let ret: i32;
    unsafe {
        result = ffi::malloc(std::mem::size_of::<f64>() as u64) as *mut f64;
        ret = ffi::gsGetRowFieldAsDouble(row, column as i32, result);
        value_result = *result;
        ffi::free(result as *mut _);
    }
    (ret, value_result)
}

fn get_row_field_as_blob(row: *mut GSRow, column: usize) -> (i32, Vec<u8>) {
    let mut vec_result: Vec<u8> = vec![];
    let result: *mut ffi::GSBlob;
    let tmp_string;
    let ret: i32;
    unsafe {
        result = ffi::malloc(std::mem::size_of::<ffi::GSBlob>() as u64) as *mut ffi::GSBlob;
        ret = ffi::gsGetRowFieldAsBlob(row, column as i32, result);
        tmp_string = str::from_utf8_unchecked(slice::from_raw_parts(
            (*result).data as *const u8,
            (*result).size.try_into().unwrap(),
        ));
    }
    unsafe {
        ffi::free(result as *mut _);
    }
    for &elem in tmp_string.as_bytes() {
        vec_result.push(elem);
    }
    (ret, vec_result)
}

fn get_row_field_as_timestamp(row: *mut GSRow, column: usize) -> (i32, Timestamp) {
    let result: *mut i64;
    let value_result: i64;
    let ret: i32;
    unsafe {
        result = ffi::malloc(std::mem::size_of::<i64>() as u64) as *mut i64;
        ret = ffi::gsGetRowFieldAsTimestamp(row, column as i32, result);
        value_result = *result;
        ffi::free(result as *mut _);
    }

    // Return DateTime
    (ret, Timestamp {
        value: value_result,
    })
}
