import * as d3 from 'https://unpkg.com/d3@4.12.2/index.js?module'
// import * as d3 from 'https://unpkg.com/d3?module'


export const lineplot = (id,data,rs,d_x,d_y,color="#69b3a2") => {
  
  
// set the dimensions and margins of the graph
var margin = {top: 10, right: 30, bottom: 100, left: 100},
    width = 800 - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom;

// append the svg object to the body of the page
var svg = d3.select(`#${id}`)
  .append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform",
          "translate(" + margin.left + "," + margin.top + ")");

//Read the data
let lst = []
  // When reading the csv, I must format variables:
    for(let i = 0; i < data.length; i++){
      if(isNaN(parseFloat(data[i][d_y]))==false){
        lst.push({ date : d3.timeParse("%Y-%m-%d")(data[i][d_x]), value : parseFloat(data[i][d_y]) })
      }

    }

  // Now I can use this dataset:
    // console.log(lst)
    data = lst
    // Add X axis --> it is a date format
    var x = d3.scaleTime()
      .domain(d3.extent(data, function(d) { return d.date; }))
      .range([ 0, width]);
    svg.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x));
    // Add Y axis
    var y = d3.scaleLinear()
      .domain( [d3.min(data, d=>d.value), d3.max(data, d=>d.value)+20])
      .range([ height, 0 ]);
    svg.append("g")
      .call(d3.axisLeft(y));
    // Add the line
    svg.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", color)
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.date) })
        .y(function(d) { return y(d.value) })
        )
    // Add the points
      // svg
      //   .append("g")
      //   .selectAll("dot")
      //   .data(data)
      //   .enter()
      //   .append("circle")
      //     .attr("cx", function(d) { return x(d.date) } )
      //     .attr("cy", function(d) { return y(d.value) } )
      //     .attr("r", 5)
      //     .attr("fill", color)

         //xlabel
         svg.append("text")
         .attr("class", "x label")
         .attr("fill", "black")
         .attr("text-anchor", "end")
         .attr("x", width-300)
         .attr("y", height+40)
         .text(d_x);

     //y label
         svg.append("text")
         .attr("class", "y label")
         .attr("fill", "black")
         .attr("text-anchor", "end")
         .attr("x", -120)
         .attr("y", -70)
         .attr("dy", ".75em")
         .attr("transform", "rotate(-90)")
         .text(d_y);


    }
    
  
