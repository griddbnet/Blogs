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

#![allow(non_snake_case)]
pub mod AggregationResult;
pub mod Const;
pub mod Container;
pub mod ContainerInfo;
pub mod Query;
pub mod RowSet;
pub mod Store;
pub mod StoreFactory;
pub mod Type;
pub mod Util;
pub mod Value;

#[macro_export]
// Support put data when put row
macro_rules! gsvec {
  () => (
      $crate::vec::Vec::new()
  );
  ($elem:expr; $n:expr) => (
      $crate::vec::from_elem(Value::from($elem), $n)
  );
  ($($x:expr),*) => (
      Box::new([$(Value::from($x)),*]).to_vec()
  );
  ($($x:expr,)*) => (gsvec![$($x),*])
}

#[macro_export]
// Support get data from Value
macro_rules! get_value {
    ($input:expr) => {
        $input.clone().into()
    };
}
