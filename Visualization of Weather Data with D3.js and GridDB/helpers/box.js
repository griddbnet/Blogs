
import * as d3 from 'https://unpkg.com/d3?module'



export const boxplot = (id,data,feature_name='',box_color="#69b3a2") => {

let newdata = []

  
// set the dimensions and margins of the graph
var margin = {top: 10, right: 30, bottom: 50, left: 100},
  width = 400 - margin.left - margin.right,
  height = 400 - margin.top - margin.bottom;

// append the svg object to the body of the page
var svg2 = d3.select(`#${id}`)
.append("svg")
  .attr("width", width + margin.left + margin.right)
  .attr("height", height + margin.top + margin.bottom)
.append("g")
  .attr("transform",
        "translate(" + margin.left + "," + margin.top + ")");


// create dummy data
// var data = [12,19,11,13,12,22,13,4,15,16,18,19,20,12,11,9]
var data = data

// get the data

// d3.csv("windspeed.csv", function(data) {   
// Compute summary statistics used for the box:
var data_sorted = data.sort(d3.ascending)
var q1 = d3.quantile(data_sorted, .25)
var median = d3.quantile(data_sorted, .5)
var q3 = d3.quantile(data_sorted, .75)
var interQuantileRange = q3 - q1
var min = q1 - 1.5 * interQuantileRange
var max = q1 + 1.5 * interQuantileRange

// Show the Y scale
var y = d3.scaleLinear()
  .domain([min*3,max*3])
  .range([height, max]);
svg2.call(d3.axisLeft(y))

// a few features for the box
var center = 200
var width = 100

// Show the main vertical line
svg2
.append("line")
  .attr("x1", center)
  .attr("x2", center)
  .attr("y1", y(min) )
  .attr("y2", y(max) )
  .attr("stroke", "black")

// Show the box
svg2
.append("rect")
  .attr("x", center - width/2)
  .attr("y", y(q3) )
  .attr("height", (y(q1)-y(q3)) )
  .attr("width", width )
  .attr("stroke", "black")
  .style("fill", box_color)

// show median, min and max horizontal lines
svg2
.selectAll("toto")
.data([min, median, max])
.enter()
.append("line")
  .attr("x1", center-width/2)
  .attr("x2", center+width/2)
  .attr("y1", function(d){ return(y(d))} )
  .attr("y2", function(d){ return(y(d))} )
  .attr("stroke", "black")

  //y label
  svg2.append("text")
  .attr("fill", "black")
  .attr("class", "y label")
  .attr("text-anchor", "end")
  .attr("x", -120)
  .attr("y", -70)
  .attr("dy", ".75em")
  .attr("transform", "rotate(-90)")
  .text(feature_name);

// Add individual points with jitter
// var jitterWidth = 50
// svg2
// .selectAll("indPoints")
// .data(data)
// .enter()
// .append("circle")
//   .attr("cx", function(d){return(120+jitterWidth + Math.random()*jitterWidth )})
//   .attr("cy", function(d){return(y(d))})
//   .attr("r", 4)
//   .style("fill", point_color)
//   .attr("stroke", "black")
  
// })

}

