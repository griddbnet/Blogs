import React, { useState, useEffect, useCallback } from 'react';
import Box from '@mui/material/Box';
import {
  GridColumns,
  GridRowId,
  GridRowsProp,
  DataGrid,
  GridCellEditStopParams,
  GridCellEditStopReasons,
  MuiEvent,
} from '@mui/x-data-grid';
import Button from '@mui/material/Button';

const columns = [
  { field: 'id', headerName: 'ID', width: 90 },
  {
    field: 'timestamp',
    headerName: 'Time',
    width: 220,
    editable: false,
  },
  {
    field: 'location',
    headerName: 'Location',
    width: 110,
    editable: true
  },
  {
    field: 'data',
    headerName: 'Data',
    width: 110,
    editable: false,
  },
  {
    field: 'temperature',
    headerName: 'Temperature',
    type: 'number',
    width: 110,
    editable: false,
  },
];


const App = () => {
  const [selectedRows, setSelectedRows] = useState([]);

  const [rows, setRows] = useState(
    [
      { id: 1, timestamp: "Generating Data", location: "A1", data: 10.10, temperature: 22.22 }
    ]
  )

  useEffect(() => {
    var xhr = new XMLHttpRequest();

    xhr.onreadystatechange = function () {

      if (xhr.readyState !== 4) return;
      if (xhr.status >= 200 && xhr.status < 300) {
        let resp = JSON.parse(xhr.responseText);
        let res = resp.results


        var t = []
        for (let i = 0; i < res.length; i++) {
          let obj = {}
          obj["id"] = i
          obj["timestamp"] = res[i][0]
          obj["location"] = res[i][1]
          obj["data"] = res[i][2].toFixed(2)
          obj["temperature"] = res[i][3]
          t.push(obj)
        }
        console.log("rows: ", rows)
        setRows(t)
      }
    };
    xhr.open('GET', '/firstLoad');
    xhr.send();

  }, [])

  useEffect(() => {
    console.log("data: ", rows)
  }, [rows])

  const updateRow = useCallback((row) => {
    console.log("Str: ", row)
    fetch('/update', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ row })
    }).then(function (response) {
      console.log(response)
      return response.json();
    });
  }, [])

  const processRowUpdate = useCallback(
    async (newRow) => {
      // Make the HTTP request to save in the backend
      console.log("new row: ", newRow)
      const response = await updateRow(newRow);
      console.log("Response from roiw update: ", response)
      return newRow;
    },
    [],
  );

  const handleProcessRowUpdateError = useCallback((error) => {
    console.log("Failed")
  }, []);

  const deleteRow = useCallback((rows) => {
    console.log("Str: ", rows)
    fetch('/delete', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ rows })
    }).then(function (response) {
      console.log(response)
      return response.json();
    });
  }, [])

  useEffect(() => {
    console.log("selectionModel: ", selectedRows)
  }, [selectedRows])

  const handleClick = useCallback(
    async (newRow) => {
      // Make the HTTP request to save in the backend
      console.log("new row: ", newRow)
      const response = await updateRow(newRow);
      console.log("Response from roiw update: ", response)
      return newRow;
    },
    [],
  );

  return (
    <div style={{ display: "flex", justifyContent: "center" }}>
      <Box sx={{
        height: 600,
        width: '50%',
        '& .MuiDataGrid-cell--editable': {
          bgcolor: (theme) =>
            theme.palette.mode === 'dark' ? '#376331' : 'rgb(217 243 190)',
        }
      }}>
        <DataGrid
          rows={rows}
          experimentalFeatures={{ newEditingApi: true }}
          isCellEditable={(params) => params.row.location}
          columns={columns}
          pageSize={10}
          processRowUpdate={processRowUpdate}
          onProcessRowUpdateError={handleProcessRowUpdateError}
          rowsPerPageOptions={[5]}
          hideFooterPagination
          checkboxSelection
          onSelectionModelChange={(ids) => {
            const selectedIDs = new Set(ids);
            const selectedRows = rows.filter((row) =>
              selectedIDs.has(row.id),
            );

            setSelectedRows(selectedRows);
          }}
        />
      </Box>
      <Button variant="contained" onClick={handleClick}>
      Delete Selected Rows
    </Button>

    </div>
  )
}

export default App;
