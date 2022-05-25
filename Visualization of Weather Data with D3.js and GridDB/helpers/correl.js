import * as d3 from 'https://unpkg.com/d3@4.12.2/index.js?module'



export const correlogram = (data) => {
  
  

  d3.select("body").append("div").attr("class", "tip").style("display", "none");


  
  
  let result = data.flatMap(Object.keys);
   let cols = [...new Set(result)];
    let lst = []
  
    //convert to numbers
    for(let i =0; i<cols.length;i++){
    for(let j =0; j<data.length;j++){
      data[j][cols[i]] = parseFloat(data[j][cols[i]])    
    } 
  }
  
  //filtering out nulls otherwise correlation becomes NA
  for(let j =0; j<data.length;j++){
      let temp = Object.values(data[j])
        let out =  temp.length - temp.filter(Number.isFinite).length
        if(out==0){
          lst.push(data[j])
        }
      
    }
  
  
  data = lst
  
  
  var corr = jz.arr.correlationMatrix(data, cols);
  var extent = d3.extent(corr.map(function(d){ return d.correlation; }).filter(function(d){ return d !== 1; }));
  
  var grid = data2grid.grid(corr);
  var rows = d3.max(grid, function(d){ return d.row; });
  
  var margin = {top: 20, bottom: 1, left: 50, right: 1};
  
  var dim = d3.min([window.innerWidth * .49, window.innerHeight * .45]);
  
  var width = dim - margin.left - margin.right, height = dim - margin.top - margin.bottom;
  
  var svg = d3.select("#grid").append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
    .append("g")
      .attr("transform", "translate(" + margin.left + ", " + margin.top + ")");
  
  var padding = .1;
  
  var x = d3.scaleBand()
    .range([0, width])
    .paddingInner(padding)
    .domain(d3.range(1, rows + 1));
  
  var y = d3.scaleBand()
    .range([0, height])
    .paddingInner(padding)
    .domain(d3.range(1, rows + 1));
  
  var c = chroma.scale(["tomato", "white", "steelblue"])
    .domain([extent[0], 0, extent[1]]);
  
  var x_axis = d3.axisTop(y).tickFormat(function(d, i){ return cols[i]; });
  var y_axis = d3.axisLeft(x).tickFormat(function(d, i){ return cols[i]; });
  
  svg.append("g")
      .attr("class", "x axis")
      .call(x_axis);
  
  svg.append("g")
      .attr("class", "y axis")
      .call(y_axis);
  
  svg.selectAll("rect")
      .data(grid, function(d){ return d.column_a + d.column_b; })
    .enter().append("rect")
      .attr("x", function(d){ return x(d.column); })
      .attr("y", function(d){ return y(d.row); })
      .attr("width", x.bandwidth())
      .attr("height", y.bandwidth())
      .style("fill", function(d){ return c(d.correlation); })
      .style("opacity", 1e-6)
    // .transition()
      .style("opacity", 1);
  
  svg.selectAll("rect")
  
  
  // legend scale
  var legend_top = 15;
  var legend_height = 15;
  
  var legend_svg = d3.select("#correlation").append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", legend_height + legend_top)
    .append("g")
      .attr("transform", "translate(" + margin.left + ", " + legend_top + ")");
  
  var defs = legend_svg.append("defs");
  
  var gradient = defs.append("linearGradient")
      .attr("id", "linear-gradient");
  
  var stops = [{offset: 0, color: "tomato", value: extent[0]}, {offset: .5, color: "white", value: 0}, {offset: 1, color: "steelblue", value: extent[1]}];
  
  gradient.selectAll("stop")
      .data(stops)
    .enter().append("stop")
      .attr("offset", function(d){ return (100 * d.offset) + "%"; })
      .attr("stop-color", function(d){ return d.color; });
  
  legend_svg.append("rect")
      .attr("width", width)
      .attr("height", legend_height)
      .style("fill", "url(#linear-gradient)");
  
  legend_svg.selectAll("text")
      .data(stops)
    .enter().append("text")
      .attr("x", function(d){ return width * d.offset; })
      .attr("dy", -3)
      .style("text-anchor", function(d, i){ return i == 0 ? "start" : i == 1 ? "middle" : "end"; })
      .text(function(d, i){ return d.value + (i == 2 ? ">" : ""); })
  
    }
    
  
