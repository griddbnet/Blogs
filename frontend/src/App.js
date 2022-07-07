import React, { useState, useEffect } from 'react';

      const App = () =>  {
        const [data, setData] = useState(null)
        const [name, setName] = useState(null)
        const [vitamins, setVitamins] = useState(null)        

        useEffect(() => {
            var xhr = new XMLHttpRequest();

            xhr.onreadystatechange = function () {

                if (xhr.readyState !== 4) return;
                if (xhr.status >= 200 && xhr.status < 300) {
                    let resp = JSON.parse(xhr.responseText);
                    setData(resp.results)
                }
            };

            xhr.open('GET', '/all');
            xhr.send();

        }, [])

        const [selectList, setSelectList] = useState(null)
        const [selectComp, setSelectComp] = useState(null)

        useEffect( () => {
            if (data !== null) {
                var namesArr = []
                var caloriesArr = []
                var proteinArr = []
                var fatArr = []
                var sodiumArr = []
                var fiberArr = []
                var carboArr = []
                var sugarsArr = []
                var vitaminsArr = []
                var listOfStuff = ['calories', 'protein', 'fat',
                                    'sodium', 'fiber', 'carbo', 'sugars',
                                    'vitamins']
                var listOfComp = ["Greater Than", "Less Than", "Equal"]

                data.forEach(row => {
                    namesArr.push(row[0])
                    caloriesArr.push(row[3])
                    proteinArr.push(row[4])
                    fatArr.push(row[5])
                    sodiumArr.push(row[6])
                    fiberArr.push(row[7])
                    carboArr.push(row[8])
                    sugarsArr.push(row[9])
                    vitaminsArr.push(row[11])
                })


                const y = listOfStuff.map((l, i) => <option value={l} key={i}> {l} </option> )
                const z = listOfComp.map((c, i) => <option value={c} key={i}> {c} </option> )

                setSelectList(y)
                setSelectComp(z)

                setName(namesArr)
                setVitamins(vitaminsArr)
            }
        }, [data])        
    

        const [selectName, setSelectName] = useState(null)

        useEffect( () => {
            if (name !== null) {
                //console.log(name, calories, protein, fat, sodium, fiber, carbo, sugars, vitamins)
                const x = name.map((n, i) =>
                    <option value={n} key={i}> {n} </option>
                )

                setSelectName(x)
            }

        }, [vitamins])

        const [list, setList] = useState("calories")
        const handleList = (event) => {
            console.log("handle List: ", event.target.value)
            setList(event.target.value)
        }
        const [comp, setComp] = useState("Greater Than")
        const handleComp = (event) => {
            console.log("handle Comp: ", event.target.value)
            setComp(event.target.value)
        }
        const [nameDropDown, setNameDropDown] = useState("100% Bran")
        const handleName = (event) => {
            console.log("handle List: ", event.target.value)
            setNameDropDown(event.target.value)
        }

        const [specific, setSpecific] = useState(null)

        const handleSubmit = async (event) => {
            fetch('/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({'list': list, 'comp': comp, 'name': nameDropDown})
            }).then(function(response) {
                console.log(response)
                return response.json();
            });
            event.preventDefault()

            let response = await fetch("/test")
            let result = await response.text()
            let resp = await JSON.parse(result)
            setSpecific(resp)
        }

        const [tableRows, setTableRows] = useState(null)
        useEffect( () => {
            if (specific !== null) {
               // console.log("specific: ", specific.userResult)
                let x = []
                specific.userResult.forEach(row => {
                    x.push(processRow(row))
                  //  console.log(row, x)
                })
                console.log("x: ", x)
                setTableRows(x)
            }
        }, [specific])

        const [dataRows, setDataRows] = useState(null)
        useEffect( () => {
            console.log('Table Rows: ' , tableRows)
            if (tableRows !== null) {
                const x = tableRows.map((r, i) =>
                    <tr key={i}> {r} </tr>
                )
                console.log("table rows: ", x)
                setDataRows(x)
            }
        }, [tableRows])

        const processRow = row => {
            const x = row.map((n, i) =>
                <td key={i}> {n} </td>
            )    
            return x
        }

        const makeTable = () => {
            return (
<table class="tg" id="table">
<thead>
  <tr>
    <td class="tg-0pky">Name</td>
    <td class="tg-0pky">Manufacturer</td>
    <td class="tg-0lax">Type</td>
    <td class="tg-0lax">Calories</td>
    <td class="tg-0lax">Protein</td>
    <td class="tg-0lax">Fat</td>
    <td class="tg-0lax">Sodium</td>
    <td class="tg-0lax">Fiber</td>
    <td class="tg-0lax">Carbos</td>
    <td class="tg-0lax">Sugars</td>
    <td class="tg-0lax">Potass</td>
    <td class="tg-0lax">Vitamins</td>
    <td class="tg-0lax">Shelf</td>
    <td class="tg-0lax">Weight</td>
    <td class="tg-0lax">Cups</td>
    <td class="tg-0lax">Rating</td>
  </tr>
    {dataRows}
</thead>
</table>
            )
        }


        return ( 
               <div>
                <form method="post" id="form" onSubmit={handleSubmit}>
                 <label for="Cereals">What Would You Like To Know?</label>
    
                    <select name="list" id="list" onChange={handleList}>
                        {selectList}
                    </select>

                    <select name="comp" id="comp" onChange={handleComp}>
                        {selectComp}
                    </select>

                    <select name="cereal" id="cereal" onChange={handleName}>
                        {selectName}
                    </select>

                    <br /><br />
                    <input type="submit" value="Submit Query" />
                </form>

                {makeTable()}`
                
            </div>
    )
      }
                

export default App;
