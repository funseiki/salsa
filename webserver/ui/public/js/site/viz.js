function Viz(height, width, margin, id) {
    if(arguments.length > 0) {
        // Constructor
        this.height = height;
        this.width = width;
        this.margin = margin;
        this.done = false;
        this.id = id;
        // Append a new group which will represent the current visualization
        this.chart = d3.select('.vis')
            .append("g")
                .attr("id", "vis-" + id)
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    }
}

Viz.prototype = {
    update: function(data) {
        if(this.done) {
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
        this.done = true;
    },
    // Convert the data to object format
    convert: function(data) {
        var dataArr = data.split(",");
        return {
            groupBy: dataArr[0],
            // Coerce the numerical values into numbers
            value: +dataArr[1],
            confidence: +dataArr[2],
            completion: +dataArr[3]
        };
    },
    // Key function: returns what will be used to uniquely identify the data
    key: function(data) {
        return data.groupBy;
    }
};
