import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import { DataGrid, GridColDef, GridValueGetterParams } from '@mui/x-data-grid';


const columns: GridColDef[] = [
  { field: 'id', headerName: 'ID', width: 90 },
  {
    field: 'timestamp',
    headerName: 'Time',
    width: 150,
    editable: true,
  },
  {
    field: 'data',
    headerName: 'Data',
    width: 150,
    editable: true,
  },
  {
    field: 'temperature',
    headerName: 'Temperature',
    type: 'number',
    width: 110,
    editable: true,
  },
];


      const App = () =>  {    
        const [rows, setRows] = useState(
        [
            {id: 1, timestamp: "Generating Data", data: 10.10, temperature: 22.22}
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
                            obj["data"] = res[i][1]
                            obj["temperature"] = res[i][2]
                            t.push(obj)
                    }
                    setRows(t)
                }
            };
            xhr.open('GET', '/firstLoad');
            xhr.send();

        }, [])

        useEffect(() => {
            console.log("data: ", rows)
        }, [rows])
    

        const handleSubmit = async (event) => {
            fetch('/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({'testing': 'testing'})
            }).then(function(response) {
                console.log(response)
                return response.json();
            });
            event.preventDefault()
        }

        return ( 
               <div>

    <Box sx={{ height: 400, width: '100%' }}>
      <DataGrid
        rows={rows}
        experimentalFeatures={{ newEditingApi: true }}
        columns={[{ field: 'name', editable: true }]}
        pageSize={10}
        rowsPerPageOptions={[5]}
        checkboxSelection
        disableSelectionOnClick
      />
    </Box>                


            </div>
    )
      }
                
export default App;
