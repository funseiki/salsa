function LineViz(height, width, margin, id, groupBy) {
    Viz.apply(this, arguments);
    this.barWidth = 50;
    this.barMargin = 5;
    this.chart.append("g")
        .attr("class", "x axis");
    this.chart.append("g")
        .attr("class", "y axis");
    this.groupBy = groupBy;
    var that = this;
    this.ready = false;
    // Copy over any existing data
    /*Viz.globalMap.forEach(function(key, dataArr) {
        var newArr = dataArr.map(function(data) {
            // Copy over the row values
            return {groupBy: data.groupBy, value: data.value, completion: data.completion, confidence: data.confidence};
        });
        that.dataMap.set(key, newArr);
    });*/
    this.myVals = Viz.globalMap.get(this.groupBy);
    if(!this.myVals) {
        this.myVals = Viz.globalMap.set(this.groupBy, []);
    }
    this.numCalls = 0;
    this.update(this.myVals);
}

extend(LineViz, Viz);

LineViz.prototype.getMin = function(d) {
    return d.value - d.confidence;
};

LineViz.prototype.getMax = function(d) {
    return d.value + d.confidence;
};

LineViz.prototype.makeScale = function(height, width, dataMap, key, graph) {
    var values = this.myVals;//dataMap.get(key);

    var scale = {};
    var max = d3.max(values, this.getMax);
    var min = d3.min(values, this.getMin);
    var min = d3.max([0, min - ((max - min)/2)]);

    // A way to determine y values
    scale.y = d3.scale.linear()
        // Map y(0) to height and y(max(data)) to 0. Scale down linearly
        .range([height, 0])
        .domain([min, max]);

    var maxWidth = d3.max([(this.barWidth+this.barMargin)*values.length, width/2]);
    // The range of x output values should be 0 to width (with a .1 padding)
    scale.x = d3.scale.linear()
        .range([0, maxWidth])
        .domain([0, values[values.length-1].completion]);//data.map(function(d) { return d.name; }));
    graph
        .attr('width', maxWidth + this.barWidth);
    return scale;
};

LineViz.prototype.makeAxes = function(scale) {
    var xAxis = d3.svg.axis()
        .scale(scale.x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(scale.y)
        .orient("left")
        .ticks(10);
    return {x: xAxis, y: yAxis};
}

LineViz.prototype.key = function(data) {
    var currentSnapshot = this.dataMap.get(data.groupBy);
    if(!currentSnapshot) {
        currentSnapshot = this.dataMap.set(data.groupBy, []);
    }
    currentSnapshot.push({groupBy: data.groupBy, value: data.value, completion: data.completion, confidence: data.confidence});

    // Use the groupBy as the key
    return data.groupBy;
};

LineViz.prototype.update = function(data) {
    //data.forEach(this.key.bind(this));
    //var groupData = this.dataMap.get(this.groupBy);
    var groupData = this.myVals;
    if(!groupData) {
        groupData = [];
    }
    if(groupData.length < 2) {
        // Nothing to graph with no data
        return;
    }

    var  path = this.chart.selectAll('path')
        .data([groupData]);

    var height = this.height,
        width = this.width,
        margin = this.margin;

    var scale = this.makeScale(height, width, this.dataMap, this.groupBy, this.graph);
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

    var line = d3.svg.line()
        .interpolate("monotone")
        .x(function(d) {
            //console.log("X", d);
            return x(d.completion);
        })
        .y(function(d) {
            //console.log("Y", d);
            return y(d.value);
        });

    var lineMin = d3.svg.line()
        .interpolate("monotone")
        .x(function(d) {
            //console.log("X", d);
            return x(d.completion);
        })
        .y(function(d) {
            //console.log("Y", d);
            return y(d.value - d.confidence);
        });

    var lineMax = d3.svg.line()
        .interpolate("monotone")
        .x(function(d) {
            //console.log("X", d);
            return x(d.completion);
        })
        .y(function(d) {
            //console.log("Y", d);
            return y(d.value + d.confidence);
        });

    if(this.numCalls == 0) {
        path.enter().append("path")
            .attr("class", "line val")
            .attr("d", line);
        path.enter().append("path")
            .attr("class", "line dotted-min")
            .attr("d", lineMin);
        path.enter().append("path")
            .attr("class", "line dotted-max")
            .attr("d", lineMax);
        this.numCalls++;
    }
    else {
        this.chart.selectAll(".line.val")
            .attr("d", line);
        this.chart.selectAll(".line.dotted-min")
            .attr("d", lineMin);
        this.chart.selectAll(".line.dotted-max")
            .attr("d", lineMax);
    }
};
