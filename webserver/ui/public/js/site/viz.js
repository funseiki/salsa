function Viz(height, width, margin, id) {
    if(arguments.length > 0) {
        console.log("These are the arguments", arguments);
        // Constructor
        this.height = height;
        this.width = width;
        this.margin = margin;
        this.isDone = false;
        this.id = id;
        console.log(id);
        // Append a new group which will represent the current visualization
        this.graph = d3.select("#"+id).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom);

        this.chart = this.graph.append("g")
            .attr("id", "vis-" + Math.random())
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        this.dataMap = d3.map();
    }
}

Viz.prototype = {
    update: function(data) {
        if(this.isDone) {
            return;
        }
        // Call this when there is data to update the visualization with
        this.data = this.convert(data);
    },
    destroy: function() {
        this.update([]);
        // Do something to remove this div
    },
    done: function() {
        this.isDone = true;
    },
    // Convert the data to object format
    convert: function(data) {
        if(!data) {
            data = [];
        }
        return data.map(function(row) {
            var dataArr = row.split(",");
            return {
                groupBy: dataArr[0],
                // Coerce the numerical values into numbers
                value: +dataArr[1],
                confidence: +dataArr[2],
                completion: +dataArr[3]
            };
        });
    },
    getVal: function(d) {
        return d.value;
    },
    // Key function: returns what will be used to uniquely identify the data
    key: function(data) {
        this.dataMap.set(data.groupBy, {value: data.value, confidence: data.confidence});
        // Use the groupby as the key
        return data.groupBy;
    }
};
