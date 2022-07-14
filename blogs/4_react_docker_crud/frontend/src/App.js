import React, { useState, useEffect } from 'react';

      const App = () =>  {    

        useEffect(() => {
            var xhr = new XMLHttpRequest();

            xhr.onreadystatechange = function () {

                if (xhr.readyState !== 4) return;
                if (xhr.status >= 200 && xhr.status < 300) {
                    let resp = JSON.parse(xhr.responseText);
                    setData(resp.results)
                }
            };

            xhr.open('GET', '/firstLoad');
            xhr.send();

        }, [])
    

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
                <form method="post" id="form" onSubmit={handleSubmit}>
                 <label for="Cereals">Sensor Data</label>
    
                    <input type="submit" value="Submit Query" />
                </form>
                
            </div>
    )
      }
                
export default App;