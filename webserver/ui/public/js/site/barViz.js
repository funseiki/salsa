function BarViz(height, width, margin, id) {
    Viz.apply(this, arguments);
    this.barWidth = 50;
    this.barMargin = 5;
    this.chart.append("g")
        .attr("class", "x axis");
    this.chart.append("g")
        .attr("class", "y axis")
        /*.append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text("Frequency");*/
}
extend(BarViz, Viz);

BarViz.prototype.vizInit = function(chart, margin, id) {
    return this.viz;
}

BarViz.prototype.makeScale = function(height, width, dataMap, graph) {
    var scale = {};
    var keys = dataMap.keys(),
        values = dataMap.values();

    // A way to determine y values
    scale.y = d3.scale.linear()
        // Map y(0) to height and y(max(data)) to 0. Scale down linearly
        .range([height, 0])
        .domain([0, d3.max(values, this.getVal)]);

    var maxWidth = d3.max([(this.barWidth+this.barMargin)*keys.length, width/2]);

    // The range of x output values should be 0 to width (with a .1 padding)
    scale.x = d3.scale.ordinal()
        .rangeRoundBands([0, maxWidth], .1)
        .domain(keys);
    graph
        .attr('width', maxWidth + scale.x.rangeBand());
    return scale;
}

BarViz.prototype.makeAxes = function(scale) {
    var xAxis = d3.svg.axis()
        .scale(scale.x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(scale.y)
        .orient("left")
        .ticks(10);
    return {x: xAxis, y: yAxis};
}

BarViz.prototype.update = function(inData){
    // Only update if we're not done yet
    if(this.isDone) {
        return;
    }

    var data = this.convert(inData);
    // Make locals
    var height = this.height,
        width = this.width,
        margin = this.margin;
    var bar = this.chart.selectAll(".bar")
            // The key function
            .data(data, this.key.bind(this));

    var scale = this.makeScale(this.height, this.width, this.dataMap, this.graph);
    var axes = this.makeAxes(scale);

    var x = scale.x;
    var y = scale.y;

    this.chart.select(".x.axis")
        .transition()
            .duration(750)
            .attr("transform", "translate(0," + height + ")")
            .call(axes.x);

    this.chart.select(".y.axis")
        .transition()
            .duration(750)
            .call(axes.y);

    bar.enter().append("rect");

    bar.attr("class", "bar update")
        .transition()
            .duration(750)
            .attr("x", function(d) {return x(d.groupBy); })
            .attr("y", function(d) { return y(d.value); })
            .attr("height", function(d) { return height - y(d.value);})
            .attr("width", x.rangeBand());

    bar.exit().attr("class", "bar")
        .transition()
            .duration(750)
            .attr("x", function(d) {return x(d.groupBy); })
            .attr("y", function(d) { return y(d.value); })
            .attr("height", function(d) { return height - y(d.value);})
            .attr("width", x.rangeBand());

};
