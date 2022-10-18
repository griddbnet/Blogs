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

use crate::griddb::Container::*;
use crate::griddb::ContainerInfo::*;
use crate::griddb::Type::*;
use crate::num_to_enum;

use std::ffi::CString;
use std::ptr;

// Constructor Class Store
pub struct Store {
    _ptr: *mut ffi::GSGridStore,
}
impl Store {
    pub fn new(store: *mut ffi::GSGridStore) -> Store {
        Store { _ptr: store }
    }

    pub fn put_container(
        &self,
        container_info: &ContainerInfo,
        modifiable: bool,
    ) -> Result<Container, i32> {
        let mut _container: *mut GSContainer = ptr::null_mut();
        let mut _gs_container_info = container_info.unwrap();
        let ret;
        let name_tmp: CString = CString::new(container_info.name.clone()).unwrap();
        unsafe {
            ret = ffi::gsPutContainerGeneralV4_3(
                self._ptr,
                name_tmp.as_ptr(),
                &_gs_container_info,
                modifiable as i8,
                &mut _container,
            );
        }
        if ret == ffi::GS_RESULT_OK as i32 {
            let mut type_list: Vec<Type> = Vec::new();
            let vec_column_info;
            unsafe {
                vec_column_info = std::slice::from_raw_parts(
                    _gs_container_info.columnInfoList,
                    _gs_container_info.columnCount as usize,
                )
                .to_vec();
            }
            for column_info in vec_column_info {
                type_list.push(num_to_enum!(
                    column_info.type_ => Type<i32>{String,Bool, Byte, Short, Integer, Long, Float, Double,Timestamp, Geometry, Blob};
                    panic!("Cannot convert number to `enum ContainerType`")
                ));
            }
            let container_data: Result<Container, i32> = Container::new(
                _container,
                num_to_enum!(
                    _gs_container_info.type_ => ContainerType<i32>{Collection, TimeSeries};
                    panic!("Cannot convert number to `enum ContainerType`")
                ),
                type_list,
            );
            match container_data {
                Ok(result) => Ok(result),
                Err(error) => Err(error),
            }
        } else {
            Err(ret)
        }
    }

    // Get container infor
    pub fn get_container_info(&self, name: &str) -> Result<ContainerInfo, i32> {
        let value_tmp = CString::new(name).expect("Error convert String to CString");
        let _name = value_tmp.as_ptr();
        let _gs_info: *mut ffi::GSContainerInfo;
        let _bool: *mut i8;
        let ret;
        let mut row_key = false;
        let vec_column_info;
        let mut tmp_vec_column_info: Vec<(&str, Type)> = vec![];
        let mut tmp_vec: Vec<(String, i32, i32)> = vec![];
        unsafe {
            // Malloc mem for output
            _gs_info = ffi::malloc(std::mem::size_of::<ffi::GSContainerInfo>() as u64)
                as *mut ffi::GSContainerInfo;
            _bool = ffi::malloc(std::mem::size_of::<i8>() as u64) as *mut i8;
            // Call function C API
            ret = ffi::gsGetContainerInfoV4_3(self._ptr, _name, _gs_info, _bool);
            ffi::free(_bool as *mut _);
        }
        if ret != ffi::GS_RESULT_OK as i32 {
            unsafe {
                ffi::free(_gs_info as *mut _);
            }
            return Err(ret);
        }
        unsafe {
            // get row key
            if (*_gs_info).rowKeyAssigned == ffi::GS_TRUE.try_into().unwrap() {
                row_key = true;
            }
            // get vec column info
            vec_column_info = std::slice::from_raw_parts(
                (*_gs_info).columnInfoList,
                (*_gs_info).columnCount as usize,
            )
            .to_vec();
            for x in vec_column_info {
                tmp_vec.push((
                    std::ffi::CStr::from_ptr(x.name as *const i8)
                        .to_string_lossy()
                        .into_owned(),
                    x.type_,
                    x.options,
                ));
            }
        }
        for i in &tmp_vec {
            tmp_vec_column_info.push((
                i.0.as_str(),
                num_to_enum!(
                    i.1 => Type<i32> { String, Bool, Byte, Short,
                        Integer, Long, Float, Double, Timestamp, Blob };
                    panic!("Cannot convert number to `enum Type`")
                ),
            ));
        }
        let container_type;
        unsafe {
            container_type = num_to_enum!(
                (*_gs_info).type_ => ContainerType<i32>{Collection, TimeSeries};
                panic!("Cannot convert number to `enum ContainerType`")
            );
            ffi::free(_gs_info as *mut _);
            Ok(ContainerInfo::ContainerInfo(
                name,
                tmp_vec_column_info,
                container_type,
                row_key,
            ))
        }
    }

    // Get container
    pub fn get_container(&self, name: &str) -> Result<Container, i32> {
        let mut container: *mut GSContainer = ptr::null_mut();
        let value_tmp = CString::new(name).expect("Error convert String to CString");
        let name = value_tmp.as_ptr();
        let mut ret;
        let _gs_info: *mut ffi::GSContainerInfo;
        let _bool: *mut i8;

        unsafe {
            ret = ffi::gsGetContainerGeneral(self._ptr, name, &mut container);
            if ret != ffi::GS_RESULT_OK as i32 {
                return Err(ret);
            }
            _gs_info = ffi::malloc(std::mem::size_of::<ffi::GSContainerInfo>() as u64)
                as *mut ffi::GSContainerInfo;
            _bool = ffi::malloc(std::mem::size_of::<i8>() as u64) as *mut i8;
            ret = ffi::gsGetContainerInfoV4_3(self._ptr, name, _gs_info, _bool);
            ffi::free(_bool as *mut _);
        }
        if ret != ffi::GS_RESULT_OK as i32 {
            unsafe {
                ffi::free(_gs_info as *mut _);
            }
            return Err(ret);
        }

        let mut type_list: Vec<Type> = Vec::new();
        let vec_column_info;
        unsafe {
            vec_column_info = std::slice::from_raw_parts(
                (*_gs_info).columnInfoList,
                (*_gs_info).columnCount as usize,
            )
            .to_vec();
        }
        for column_info in vec_column_info {
            type_list.push(num_to_enum!(
                    column_info.type_ => Type<i32>{String,Bool, Byte, Short, Integer, Long, Float, Double,Timestamp, Geometry, Blob};
                    panic!("Cannot convert number to `enum ContainerType`")
            ));
        }
        if ret == ffi::GS_RESULT_OK as i32 {
            let cont_type;
            unsafe {
                cont_type = num_to_enum!(
                    (*_gs_info).type_ => ContainerType<i32>{Collection, TimeSeries};
                    panic!("Cannot convert number to `enum ContainerType`")
                );
                ffi::free(_gs_info as *mut _);
            }
            let container_data: Result<Container, i32> =
                Container::new(container, cont_type, type_list);
            match container_data {
                Ok(result) => Ok(result),
                Err(error) => Err(error),
            }
        } else {
            unsafe {
                ffi::free(_gs_info as *mut _);
            }
            Err(ret)
        }
    }

    // Drop container
    pub fn drop_container(&self, name: &str) -> i32 {
        let value_tmp = CString::new(name).expect("Error convert value to CString");
        let name_ptr = value_tmp.as_ptr();
        let ret;
        unsafe {
            ret = ffi::gsDropContainer(self._ptr, name_ptr);
        }
        ret
    }
}

// Destructor
impl Drop for Store {
    fn drop(&mut self) {
        let all_related = ffi::GS_TRUE;
        unsafe {
            ffi::gsCloseGridStore(&mut self._ptr, all_related as i8);
        }
    }
}
