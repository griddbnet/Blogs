import React, { useState, useEffect, useCallback } from 'react';
import Box from '@mui/material/Box';
import { DataGrid } from '@mui/x-data-grid';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Snackbar from '@mui/material/Snackbar';
import Alert, { AlertProps } from '@mui/material/Alert';

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

  const [snackbar, setSnackbar] = useState(null);

  const handleCloseSnackbar = () => setSnackbar(null);

  const [rows, setRows] = useState(
    [
      { id: "", timestamp: "Generating Data...", location: "", data: "", temperature: "" }
    ]
  )

  const queryForRows = (endPoint) => {
    var xhr = new XMLHttpRequest();
    console.log("endpoint: ", endPoint)

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
        //console.log("rows: ", rows)
        setRows([...t])
      }
    };
    xhr.open('GET', endPoint);
    xhr.send();
  }

  useEffect(() => { //Runs on every page load
    queryForRows("/firstLoad");
  }, [])


  useEffect( () => {
    console.log("new rows: ", rows)
    
  }, [rows])

  const updateRow = useCallback((row) => {
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
      setSnackbar({ children: `New location of  ${newRow.location} successfully saved to row: ${newRow.id}`, severity: 'success' });
      return newRow;
    },
    [],
  );


  const handleProcessRowUpdateError = useCallback((error) => {
    setSnackbar({ children: error.message, severity: 'error' });
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

  const handleDeleteRow = async () => {
    await deleteRow(selectedRows);
    queryForRows("/updateRows");
  }

  const createRow = useCallback(() => {
    fetch('/create', {
      method: 'GET',
    }).then(function (response) {
      console.log(response)
      return response.json();
    });
  }, [])

  const handleCreateRow = async () => {
    await createRow();
    queryForRows("/updateRows");
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
        <h1> IoT Web App with GERN stack </h1>
        <Stack direction="row" spacing={1}>
          <Button size="small" variant="outlined" onClick={handleDeleteRow}>
            Delete selected rows
          </Button>
          <Button size="small" variant="outlined" onClick={handleCreateRow}>
            Create Row
          </Button>
        </Stack>
        <DataGrid
          rows={rows}
          experimentalFeatures={{ newEditingApi: true }}
          isCellEditable={(params) => params.row.location}
          columns={columns}
          pageSize={30}
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

      {!!snackbar && (
        <Snackbar
          open
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
          onClose={handleCloseSnackbar}
          autoHideDuration={6000}
        >
          <Alert {...snackbar} onClose={handleCloseSnackbar} />
        </Snackbar>
      )}
    </div>
  )
}

export default App;
