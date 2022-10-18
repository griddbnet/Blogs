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
use crate::griddb::Query::*;
use crate::griddb::Type::*;
use crate::griddb::Util::*;
use crate::griddb::Value::*;
use std::any::Any;
use std::ffi::CString;
use std::ptr;

pub type GSContainer = ffi::GSContainerTag;
pub type GSRow = ffi::GSRowTag;
pub type GSBlob = ffi::GSBlobTag;

// Class Container
pub struct Container {
    _ptr: *mut GSContainer,
    // _row attribute support query data
    _row: *mut GSRow,
    _type_list: Vec<Type>,
    pub container_type: ContainerType,
}
#[allow(clippy::not_unsafe_ptr_arg_deref)]
impl Container {
    pub fn new(
        container: *mut GSContainer,
        container_type: ContainerType,
        type_list: Vec<Type>,
    ) -> Result<Container, i32> {
        let mut gs_row: *mut GSRow = ptr::null_mut();
        let ret;
        unsafe {
            ret = ffi::gsCreateRowByContainer(container, &mut gs_row);
        }
        if ret == ffi::GS_RESULT_OK as i32 {
            Ok(Container {
                _ptr: container,
                _row: gs_row,
                _type_list: type_list,
                container_type,
            })
        } else {
            Err(ret)
        }
    }

    // Container get row
    fn get_row_by_string(&self, value: &str) -> (i32, i8) {
        let key_tmp = CString::new(value).unwrap();
        let mut b_exist: ffi::GSBool = ffi::GS_TRUE as i8;
        let b_exist_ptr: *mut ffi::GSBool = &mut b_exist;
        let ret;
        let for_update = ffi::GS_FALSE;

        unsafe {
            ret = ffi::gsGetRowByString(
                self._ptr,
                key_tmp.as_ptr(),
                self._row as *mut std::ffi::c_void,
                for_update as i8,
                b_exist_ptr,
            );
        }

        (ret, b_exist)
    }

    fn get_row_by_integer(&self, value: i32) -> (i32, i8) {
        let mut b_exist: ffi::GSBool = ffi::GS_TRUE as i8;
        let b_exist_ptr: *mut ffi::GSBool = &mut b_exist;
        let ret;
        let for_update = ffi::GS_FALSE;
        unsafe {
            ret = ffi::gsGetRowByInteger(
                self._ptr,
                value,
                self._row as *mut std::ffi::c_void,
                for_update as i8,
                b_exist_ptr,
            );
        }
        (ret, b_exist)
    }

    fn get_row_by_long(&self, value: i64) -> (i32, i8) {
        let mut b_exist: ffi::GSBool = ffi::GS_TRUE as i8;
        let b_exist_ptr: *mut ffi::GSBool = &mut b_exist;
        let ret;
        let for_update = ffi::GS_FALSE;
        unsafe {
            ret = ffi::gsGetRowByLong(
                self._ptr,
                value,
                self._row as *mut std::ffi::c_void,
                for_update as i8,
                b_exist_ptr,
            );
        }
        (ret, b_exist)
    }

    fn get_row_by_timestamp(&self, value: i64) -> (i32, i8) {
        let mut b_exist: ffi::GSBool = ffi::GS_TRUE as i8;
        let b_exist_ptr: *mut ffi::GSBool = &mut b_exist;
        let ret;
        let for_update = ffi::GS_FALSE;
        unsafe {
            ret = ffi::gsGetRowByTimestamp(
                self._ptr,
                value,
                self._row as *mut std::ffi::c_void,
                for_update as i8,
                b_exist_ptr,
            );
        }
        (ret, b_exist)
    }

    pub fn get<T: Any>(&self, value: T) -> Result<Vec<Value>, i32> {
        let value_any = &value as &dyn Any;
        let mut ret: i32;
        let row_exist: i8;

        let mut vec = Vec::new();
        match self._type_list[0] {
            Type::String => {
                match value_any.downcast_ref::<&str>() {
                    Some(as_string) => {
                        let (ret_func, exist) = self.get_row_by_string(*as_string);
                        ret = ret_func;
                        row_exist = exist;
                    }
                    None => {
                        return Err(ERROR_CONVERT_DATA);
                    }
                };
            }
            Type::Long => {
                match value_any.downcast_ref::<i64>() {
                    Some(value) => {
                        let (ret_func, exist) = self.get_row_by_long(*value);
                        ret = ret_func;
                        row_exist = exist;
                    }
                    None => return Err(ERROR_CONVERT_DATA),
                };
            }

            Type::Integer => {
                match value_any.downcast_ref::<i32>() {
                    Some(value) => {
                        let (ret_func, exist) = self.get_row_by_integer(*value);
                        ret = ret_func;
                        row_exist = exist;
                    }
                    None => return Err(ERROR_CONVERT_DATA),
                };
            }
            Type::Timestamp => {
                match value_any.downcast_ref::<Timestamp>() {
                    Some(value) => {
                        let (ret_func, exist) = self.get_row_by_timestamp((*value).value);
                        ret = ret_func;
                        row_exist = exist;
                    }
                    None => return Err(ERROR_CONVERT_DATA),
                };
            }
            _ => {
                return Err(ERROR_CONVERT_DATA);
            }
        }

        if ret != ffi::GS_RESULT_OK as i32 {
            return Err(ret);
        }
        if row_exist != ffi::GS_TRUE as i8 {
            // When row is not existed, return empty vector
            return Ok(vec);
        }
        ret = Util::get_row_data(self._row, &mut vec, &self._type_list);
        if ret != ffi::GS_RESULT_OK as i32 {
            Err(ret)
        } else {
            Ok(vec)
        }
    }
    // container query
    pub fn query(&self, query: &str) -> Result<Query, i32> {
        let mut p_query: *mut ffi::GSQuery = ptr::null_mut();
        let value = CString::new(query).unwrap();
        let ret;
        unsafe {
            ret = ffi::gsQuery(self._ptr, value.as_ptr(), &mut p_query);
        }
        if ret == ffi::GS_RESULT_OK as i32 {
            Ok(Query::new(p_query, self._row, self._type_list.to_vec()))
        } else {
            Err(ret)
        }
    }
    pub fn create_index(&self, name: &str, flags: IndexType) -> i32 {
        let c_string = CString::new(name).unwrap();
        let ret;
        unsafe {
            ret = ffi::gsCreateIndex(self._ptr, c_string.as_ptr(), flags as i32);
        }
        ret
    }

    pub fn drop_index(&self, name: &str, flags: IndexType) -> i32 {
        let c_string = CString::new(name).unwrap();
        let ret;
        unsafe {
            ret = ffi::gsDropIndex(self._ptr, c_string.as_ptr(), flags as i32);
        }
        ret
    }

    pub fn set_auto_commit(&self, enabled: bool) -> i32 {
        let gs_enabled: ffi::GSBool = if enabled {
            ffi::GS_TRUE as i8
        } else {
            ffi::GS_FALSE as i8
        };
        let ret;
        unsafe {
            ret = ffi::gsSetAutoCommit(self._ptr, gs_enabled);
        }
        ret
    }

    pub fn commit(&self) -> i32 {
        let ret;
        unsafe {
            ret = ffi::gsCommit(self._ptr);
        }
        ret
    }

    fn remove_by_string(&self, value: &str) -> i32 {
        let key_tmp = CString::new(value).unwrap();
        let b_exit: *mut ffi::GSBool = ptr::null_mut();
        let ret;
        unsafe {
            ret = ffi::gsRemoveRowByString(self._ptr, key_tmp.as_ptr(), b_exit);
        }
        ret
    }

    fn remove_by_integer(&self, value: i32) -> i32 {
        let b_exit: *mut ffi::GSBool = ptr::null_mut();
        let ret;
        unsafe {
            ret = ffi::gsDeleteRowByInteger(self._ptr, value, b_exit);
        }
        ret
    }

    fn remove_by_long(&self, value: i64) -> i32 {
        let b_exit: *mut ffi::GSBool = ptr::null_mut();
        let ret;
        unsafe {
            ret = ffi::gsDeleteRowByLong(self._ptr, value, b_exit);
        }
        ret
    }

    fn remove_by_timestamp(&self, value: i64) -> i32 {
        let b_exit: *mut ffi::GSBool = ptr::null_mut();
        let ret;
        unsafe {
            ret = ffi::gsDeleteRowByTimestamp(self._ptr, value, b_exit);
        }
        ret
    }

    pub fn remove<T: Any>(&self, value: T) -> i32 {
        let value_any = &value as &dyn Any;
        match self._type_list[0] {
            Type::String => match value_any.downcast_ref::<&str>() {
                Some(as_string) => self.remove_by_string(*as_string),
                None => ERROR_CONVERT_DATA,
            },
            Type::Integer => match value_any.downcast_ref::<i32>() {
                Some(value) => self.remove_by_integer(*value),
                None => ERROR_CONVERT_DATA,
            },

            Type::Long => match value_any.downcast_ref::<i64>() {
                Some(value) => self.remove_by_long(*value),
                None => ERROR_CONVERT_DATA,
            },
            Type::Timestamp => match value_any.downcast_ref::<Timestamp>() {
                Some(value) => self.remove_by_timestamp((*value).value),
                None => ERROR_CONVERT_DATA,
            },
            _ => ERROR_CONVERT_DATA,
        }
    }

    pub fn flush(&self) -> i32 {
        let ret;
        unsafe {
            ret = ffi::gsFlush(self._ptr);
        }
        ret
    }

    pub fn abort(&self) -> i32 {
        let ret;
        unsafe {
            ret = ffi::gsAbort(self._ptr);
        }
        ret
    }
    pub fn put(&self, fields: Vec<Value>) -> i32 {
        if fields.len() != self._type_list.len() {
            return ERROR_CONVERT_DATA;
        }
        let row = self._row;
        for (pos, e) in fields.iter().enumerate() {
            if e.data_type() != self._type_list[pos] {
                return ERROR_CONVERT_DATA;
            }
            e.bind(pos as i32, row);
        }

        let key = ptr::null_mut();
        let b_exit = ptr::null_mut();
        let result;
        unsafe {
            result = ffi::gsPutRow(self._ptr, key, row as *const std::ffi::c_void, b_exit);
        }
        result
    }
}

// Destructor
impl Drop for Container {
    fn drop(&mut self) {
        let all_related = ffi::GS_FALSE;
        unsafe {
            ffi::gsCloseRow(&mut self._row);
            ffi::gsCloseContainer(&mut self._ptr, all_related as i8);
        }
    }
}
