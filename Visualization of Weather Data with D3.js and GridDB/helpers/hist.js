

import * as d3 from 'https://unpkg.com/d3?module'


export const histogram = (id,data,feature_name='',color="#4169e1") => {
  
  
    // set the dimensions and margins of the graph
    var margin = {top: 10, right: 50, bottom: 50, left: 100},
        width = 600,
        height = 400;
    
    // append the svg object to the body of the page
    var svg = d3.select(`#${id}`)
      .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
      .append("g")
        .attr("transform",
              "translate(" + margin.left + "," + margin.top + ")");
    
    // get the data    
    
      // X axis: scale and draw:
      var x = d3.scaleLinear()
          .domain([0, 100])     // can use this instead of 1000 to have the max of data: d3.max(data, function(d) { return +d.price })
          .range([0, width+500]);
      svg.append("g")
          .attr("transform", "translate(0," + height + ")")
          .call(d3.axisBottom(x));
    
      // set the parameters for the histogram
      var histogram = d3.histogram()
          .value(function(d) { return d; })   // I need to give the vector of value
          .domain(x.domain())  // then the domain of the graphic
          .thresholds(x.ticks(70)); // then the numbers of bins
    
      // And apply this function to data to get the bins
      var bins = histogram(data);
    
      // Y axis: scale and draw:
      var y = d3.scaleLinear()
          .range([height, 0]);
          y.domain([0, d3.max(bins, function(d) { return d.length; })]);   // d3.hist has to be called before the Y axis obviously
      svg.append("g")
          .call(d3.axisLeft(y));
    
      // append the bar rectangles to the svg element
      svg.selectAll("rect")
          .data(bins)
          .enter()
          .append("rect")
            .attr("x", 1)
            .attr("transform", function(d) { return "translate(" + x(d.x0) + "," + y(d.length) + ")"; })
            .attr("width", function(d) { return x(d.x1) - x(d.x0) -1 ; })
            .attr("height", function(d) { return height - y(d.length); })
            .style("fill", color)

        //xlabel
        svg.append("text")
            .attr("class", "x label")
            .attr("fill", "black")
            .attr("text-anchor", "end")
            .attr("x", width-250)
            .attr("y", height+40)
            .text(feature_name);

        //y label
            svg.append("text")
            .attr("class", "y label")
            .attr("fill", "black")
            .attr("text-anchor", "end")
            .attr("x", -120)
            .attr("y", -70)
            .attr("dy", ".75em")
            .attr("transform", "rotate(-90)")
            .text("Frequency");
    
    }
    
  
