import * as d3 from 'https://unpkg.com/d3@4.12.2/index.js?module'



export const tables = (id,data,result,top=false) => {    

        function tabulate(id,data, columns,flag) {
              if(flag==true){
                data = data.slice(0,5)
              }
              var table = d3.select(`#${id}`).append('table')
              var thead = table.append('thead')
              var	tbody = table.append('tbody');
      
              // append the header row
              thead.append('tr')
                .selectAll('th')
                .data(columns).enter()
                .append('th')
                  .text(function (column) { return column; });
      
              // create a row for each object in the data
              var rows = tbody.selectAll('tr')
                .data(data)
                .enter()
                .append('tr');
      
              // create a cell in each row for each column
              var cells = rows.selectAll('td')
                .data(function (row) {
                  return columns.map(function (column) {
                    return {column: column, value: row[column]};
                  });
                })
                .enter()
                .append('td')
                  .text(function (d) { return d.value; });
      
            return table;
          }
          // render the table(s)
          tabulate(id,data, result,top); // 2 column table
      

}