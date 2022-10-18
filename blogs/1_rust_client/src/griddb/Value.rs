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

use crate::griddb::Const::*;
use crate::griddb::Type::*;
use std::ffi::CString;
extern crate griddb_sys as ffi;
pub type GSRow = ffi::GSRowTag;

#[derive(Debug, Copy, Clone)]
pub struct Timestamp {
    pub value: i64,
}

#[derive(Debug, Clone)]
pub struct Geometry {
    value: String,
}

pub struct Null {}

#[derive(Debug, Clone)]
//Support map data when put/get row
pub enum Value {
    // Null,
    Str(String),
    Bool(bool),
    Byte(i8),
    Short(i16),
    Integer(i32),
    Long(i64),
    Float(f32),
    Double(f64),
    Timestamp(Timestamp),
    Geometry(Geometry),
    Blob(Vec<u8>),
}

impl Value {
    pub fn new<A>(args: A) -> Value
    where
        A: Into<Value>,
    {
        args.into()
    }

    #[inline]
    #[must_use]
    pub fn data_type(&self) -> Type {
        match *self {
            // Value::Null => Type::Null,
            Value::Str(_) => Type::String,
            Value::Bool(_) => Type::Bool,
            Value::Byte(_) => Type::Byte,
            Value::Short(_) => Type::Short,
            Value::Integer(_) => Type::Integer,
            Value::Long(_) => Type::Long,
            Value::Float(_) => Type::Float,
            Value::Double(_) => Type::Double,
            Value::Timestamp(_) => Type::Timestamp,
            Value::Geometry(_) => Type::Geometry,
            Value::Blob(_) => Type::Blob,
        }
    }
}

impl From<String> for Geometry {
    fn from(item: String) -> Self {
        Geometry { value: item }
    }
}

impl From<Value> for Geometry {
    fn from(_item: Value) -> Geometry {
        match _item {
            Value::Geometry(val) => val,
            _ => Geometry {
                value: String::from(""),
            },
        }
    }
}

impl From<String> for Value {
    fn from(item: String) -> Self {
        Value::Str(item)
    }
}

impl From<Value> for String {
    fn from(_item: Value) -> String {
        // false
        match _item {
            Value::Str(val) => val,
            _ => String::from(""),
        }
    }
}

impl From<bool> for Value {
    fn from(item: bool) -> Self {
        Value::Bool(item)
    }
}

impl From<Value> for bool {
    fn from(_item: Value) -> bool {
        // false
        match _item {
            Value::Bool(a) => a,
            _ => false,
        }
    }
}

impl From<i8> for Value {
    fn from(item: i8) -> Self {
        Value::Byte(item)
    }
}

impl From<Value> for i8 {
    fn from(_item: Value) -> i8 {
        // false
        match _item {
            Value::Byte(val) => val,
            _ => ERROR_CONVERT_DATA as i8,
        }
    }
}

impl From<i16> for Value {
    fn from(item: i16) -> Self {
        Value::Short(item)
    }
}

impl From<Value> for i16 {
    fn from(_item: Value) -> i16 {
        // false
        match _item {
            Value::Short(val) => val,
            _ => ERROR_CONVERT_DATA as i16,
        }
    }
}

impl From<i32> for Value {
    fn from(item: i32) -> Self {
        Value::Integer(item)
    }
}

impl From<Value> for i32 {
    fn from(_item: Value) -> i32 {
        // false
        match _item {
            Value::Integer(int) => int,
            _ => ERROR_CONVERT_DATA as i32,
        }
    }
}

impl From<i64> for Value {
    fn from(item: i64) -> Self {
        Value::Long(item)
    }
}

impl From<Value> for i64 {
    fn from(_item: Value) -> i64 {
        // false
        match _item {
            Value::Long(val) => val,
            _ => ERROR_CONVERT_DATA as i64,
        }
    }
}

impl From<f32> for Value {
    fn from(item: f32) -> Self {
        Value::Float(item)
    }
}

impl From<Value> for f32 {
    fn from(_item: Value) -> f32 {
        match _item {
            Value::Float(val) => val,
            _ => ERROR_CONVERT_DATA as f32,
        }
    }
}

impl From<f64> for Value {
    fn from(item: f64) -> Self {
        Value::Double(item)
    }
}

impl From<Value> for f64 {
    fn from(_item: Value) -> f64 {
        match _item {
            Value::Double(val) => val,
            _ => ERROR_CONVERT_DATA as f64,
        }
    }
}

impl From<Timestamp> for Value {
    fn from(item: Timestamp) -> Self {
        Value::Timestamp(item)
    }
}

impl From<Value> for Timestamp {
    fn from(_item: Value) -> Timestamp {
        match _item {
            Value::Timestamp(val) => val,
            _ => Timestamp {
                value: ERROR_CONVERT_DATA as i64,
            },
        }
    }
}

impl From<Vec<u8>> for Value {
    fn from(item: Vec<u8>) -> Self {
        Value::Blob(item)
    }
}

impl From<Value> for Vec<u8> {
    fn from(_item: Value) -> Vec<u8> {
        match _item {
            Value::Blob(val) => val,
            _ => Vec::new(),
        }
    }
}

pub trait FieldBinder {
    fn bind(&self, column: i32, row: *mut GSRow) -> i32;
}

#[allow(clippy::not_unsafe_ptr_arg_deref)]
impl FieldBinder for Value {
    fn bind(&self, column: i32, row: *mut GSRow) -> i32 {
        match self {
            Value::Str(a) => {
                let key_tmp = CString::new((*a).clone()).unwrap();
                let key_ptr = key_tmp.as_ptr();
                unsafe { ffi::gsSetRowFieldByString(row, column, key_ptr) }
            }
            Value::Bool(a) => unsafe { ffi::gsSetRowFieldByBool(row, column, *a as i8) },
            Value::Byte(a) => unsafe { ffi::gsSetRowFieldByByte(row, column, *a) },
            Value::Short(a) => unsafe { ffi::gsSetRowFieldByShort(row, column, *a) },
            Value::Integer(a) => unsafe { ffi::gsSetRowFieldByInteger(row, column, *a) },
            Value::Long(a) => unsafe { ffi::gsSetRowFieldByLong(row, column, *a) },
            Value::Float(a) => unsafe { ffi::gsSetRowFieldByFloat(row, column, *a) },
            Value::Double(a) => unsafe { ffi::gsSetRowFieldByDouble(row, column, *a) },
            Value::Timestamp(a) => unsafe { ffi::gsSetRowFieldByTimestamp(row, column, a.value) },
            Value::Geometry(a) => unsafe {
                ffi::gsSetRowFieldByGeometry(row, column, a.value.as_ptr() as *const i8)
            },
            Value::Blob(a) => {
                let vec_blob: Vec<ffi::GSBlob> = vec![ffi::GSBlob {
                    size: a.len() as u64,
                    data: a.as_ptr() as *const std::ffi::c_void,
                }];
                unsafe { ffi::gsSetRowFieldByBlob(row, column, vec_blob.as_ptr()) }
            }
        }
    }
}
