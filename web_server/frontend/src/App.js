import React, { useState, useEffect, useCallback } from 'react';
import axios from "axios";

import Box from '@mui/material/Box';
import { DataGrid } from '@mui/x-data-grid';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';

const sampleTodo = [
  { id: 0, title: "Test", completed: false }
]

const columns = [
  { field: 'Title', headerName: "Title", editable: false, width: 300 },
  { field: 'Completed', headerName: "Completed", editable: false }
]


const App = () => {

  const [rows, setRows] = useState(sampleTodo)
  const [todoTitle, setTodoTitle] = useState("Create Todo Application")
  const [lastId, setLastId] = useState(0)
  const [token, setToken] = useState(null)

  const getToken = () => {
    fetch("/getToken").then(function (response) {
      return response.text()
    }).then((data) => {
        let respToken = data
        console.log("respToken: ", data)
        setToken(respToken)
      }) 
    }


const queryForRows = () => {
  axios.get("/get",{ headers:{"Token": token}}).then((response) => {
    let resp = (response.data);
    let lastId = 0
    resp.forEach((val, idx) => {
      val["id"] = idx //adding ids for the datagrid (separate from the GridDB rowkey)
      lastId = val.Id // grab last Id
    })
    setRows([...resp])
    setLastId(lastId + 1)
  })
}

useEffect(() => { //Runs on every page load
  getToken();
}, [])

useEffect(() => {
  if (token) {
    queryForRows();
  }
}, [token])

const createTodoItem = () => {
  if (todoTitle) {
    fetch('/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Token': token,
      },
      body: JSON.stringify({ Id: lastId, Title: todoTitle, Completed: false })
    }).then(function (response) {
      console.log(response)
      queryForRows();
      setTodoTitle("");
    });
  }
}

const toggleTodo = row => {
  console.log("toggle todo row: ", row.row)
  fetch("/update/" + row.row.Id, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Token': token,
    },
  }).then((response) => {
    console.log("Response: ", response)
    queryForRows();
  })
}


return (
  <div style={{ display: "flex", justifyContent: "center" }}>
    <Box sx={{
      height: 900,
      width: '50%',
      '& .MuiDataGrid-cell--editable': {
        bgcolor: (theme) =>
          theme.palette.mode === 'dark' ? '#376331' : 'rgb(217 243 190)',
      }
    }}>
      <h1> GridDB Todo App </h1>
      <Stack direction="row" spacing={1}>
        <Button size="small" variant="outlined" onClick={createTodoItem} >
          Create Todo Item
        </Button>
        <TextField
          id="standard-basic"
          label="Todo Item"
          variant="outlined"
          value={todoTitle}
          onChange={(event) => {
            setTodoTitle(event.target.value);
          }}
        />
      </Stack>
      <DataGrid
        sx={{
          ".completed": {
            bgcolor: "darkgrey",
            textDecorationLine: "line-through",
            textTransform: 'uppercase'
          },
          ".incomplete": {
            bgcolor: "#f7e7ce",
            textTransform: 'uppercase'
          },
        }}
        rows={rows}
        experimentalFeatures={{ newEditingApi: true }}
        columns={columns}
        pageSize={20}
        // processRowUpdate={processRowUpdate}
        // onProcessRowUpdateError={handleProcessRowUpdateError}
        rowsPerPageOptions={[5]}
        hideFooterPagination
        density='compact'
        onCellClick={toggleTodo}
        getRowClassName={(params) => {
          return params.row.Completed ? "completed" : "incomplete";
        }}
      //  onSelectionModelChange={(ids) => {
      //    const selectedIDs = new Set(ids);
      //    const selectedRows = rows.filter((row) =>
      //       selectedIDs.has(row.id),
      //      );

      //        setSelectedRows(selectedRows);
      //     }}
      />
    </Box>

  </div>
)
}

export default App;
