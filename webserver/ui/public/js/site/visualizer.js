
data =  [[1,40,5,6],[4,50,7,8]];

var chart;
var w = 20,
    h = 80;
           
var x = d3.scale.linear()
    .domain([0, 1])
    .range([0, w]);
           
var y = d3.scale.linear()
    .domain([0, 100])
    .rangeRound([0, h]);

// Singleton which will handle visualizing things
var visualizer = {
    clear: function() {
        // TODO: clear the visualization
    },
    startViz: function(shape, params) {
                
        chart = d3.select(".vis").append("svg")
              .attr("class", "chart")
              .attr("width", w*30 )
              .attr("height", h);

        chart.selectAll("rect")
              .data(data)
            .enter().append("rect")
              .attr("x", function(d, i) { return x(i) - .5; })
              .attr("y", function(d) { return h - y(d[1]) - .5; })
              .attr("width", w)
              .attr("height", function(d) { return y(d[1]); });

        chart.append("line")
              .attr("x1", 0)
              .attr("x2", w * data.length)
              .attr("y1", h - .5)
              .attr("y2", h - .5)
              .style("stroke", "#000");

    },
    update: function(data) {
        // Call this when there is data to update the visualization with
        console.log(data);
        chart.selectAll("rect")
        .data(data)
      .transition()
        .duration(1000)
        .attr("y", function(d) { 
            return h - y(d[1]) - .5; })
        .attr("height", function(d) {
            return y(d[1]); });


    },
    done: function() {
        // Call this when the visualization is complete
    }
}


visualizer.startViz();
visualizer.update(data);

/*setInterval(function() {
   data.shift();
   data.push(next());
   visualizer.update(data);
 }, 1500);*/



