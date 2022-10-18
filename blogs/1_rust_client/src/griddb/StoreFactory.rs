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

use crate::griddb::Store::*;
use crate::griddb::Util::*;

use std::ffi::CString;
use std::ptr;
use std::vec::Vec;

const RUST_CLIENT_VERSION: &str = "GridDB Rust Client Version 0.5";
pub type GSGridStore = ffi::GSGridStoreTag;
pub struct StoreFactory {
    _ptr: *mut ffi::GSGridStoreFactory,
}

impl StoreFactory {
    // Get Default Factory
    pub fn get_instance() -> StoreFactory {
        let _factory;
        unsafe {
            _factory = ffi::gsGetDefaultFactory();
        }
        StoreFactory { _ptr: _factory }
    }

    // get Store Factory
    pub fn get_store(&self, properties: Vec<(&str, &str)>) -> Result<Store, i32> {
        let mut temp_vec: Vec<CString> = Vec::new();
        let _properties = Util::tup_to_properties(properties, &mut temp_vec);
        let mut _store: *mut ffi::GSGridStore = ptr::null_mut();
        let ret;
        let property_count = _properties.iter().len();
        unsafe {
            ret = ffi::gsGetGridStore(
                self._ptr,
                _properties.as_ptr(),
                property_count as u64,
                &mut _store,
            );
        }
        drop(temp_vec);
        if ret == ffi::GS_RESULT_OK as i32 {
            Ok(Store::new(_store))
        } else {
            Err(ret)
        }
    }

    // Get version GridDB rust client
    pub fn get_version() -> String {
        String::from(RUST_CLIENT_VERSION)
    }
}

impl Drop for StoreFactory {
    fn drop(&mut self) {
        let all_related = ffi::GS_FALSE;
        unsafe {
            ffi::gsCloseFactory(&mut self._ptr, all_related as i8);
        }
    }
}
