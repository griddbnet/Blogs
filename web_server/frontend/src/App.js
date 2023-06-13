import React, { useState, useEffect, useLayoutEffect, useRef } from 'react';
import axios from "axios";

import Box from '@mui/material/Box';
import { DataGrid } from '@mui/x-data-grid';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import DeleteIcon from '@mui/icons-material/DeleteOutlined';

const sampleTodo = [
  { id: 0, title: "Test", completed: false }
]



const App = () => {

  const [rows, setRows] = useState(sampleTodo)
  const [todoTitle, setTodoTitle] = useState("Create Todo Application")
  const [lastId, setLastId] = useState(0)
  const [token, setToken] = useState(null)

  const columns = [
    { field: 'Title', headerName: "Title", editable: false, width: 300 },
    { field: 'Completed', headerName: "Completed", editable: false },
    {
      field: 'Delete',
      headerName: 'Delete',
      width: 150,
      renderCell: Delete,
    },
  ]

  function Delete(props) {
    const { hasFocus, value } = props;
    const buttonElement = useRef(null);
    const rippleRef = useRef(null);

    useLayoutEffect(() => {
      if (hasFocus) {
        const input = buttonElement.current?.querySelector('input');
        input?.focus();
      } else if (rippleRef.current) {
        rippleRef.current.stop({});
      }
    }, [hasFocus]);

    return (
      <strong>
        <Button
          component="button"
          ref={buttonElement}
          touchRippleRef={rippleRef}
          variant="contained"
          size="small"
          style={{ marginLeft: 16 }}
          tabIndex={hasFocus ? 0 : -1}
          onClick={deleteTodoItem}
          onKeyDown={(event) => {
            if (event.key === ' ') {
              event.stopPropagation();
            }
          }}
        >
          <DeleteIcon />
        </Button>
      </strong>
    );
  }



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
    axios.get("/get", { headers: { "Token": token } }).then((response) => {
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

  const deleteTodoItem = () => {

    console.log("delete todo row: ", workingRow)
    if (workingRow.Id > -1) {
      fetch('/delete/' + workingRow.Id, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Token': token,
        },
      }).then(function (response) {
        console.log("Delete Response: ", response)
        queryForRows();
      });
    }

  }

  const [workingRow, setWorkingRow] = useState({ Id: -1, Completed: false })
  const toggleTodo = row => {
    console.log("toggle todo row: ", row.row)
    setWorkingRow(row.row)
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
          rowsPerPageOptions={[5]}
          hideFooterPagination
          density='compact'
          onCellClick={toggleTodo}
          getRowClassName={(params) => {
            return params.row.Completed ? "completed" : "incomplete";
          }}
        />
      </Box>

    </div>
  )
}

export default App;
