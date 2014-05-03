var margin = {top: 20, right: 30, bottom: 30, left: 40}
    width = 500 - margin.left - margin.right,
    height = 200 - margin.top - margin.bottom;

var vis = d3.select('.vis')
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)

// Singleton which will handle visualizing things
var visualizer = {
    clear: function() {
        // TODO: clear the visualization
    },
    addGraph: function(shape, params) {
        if(!this.graphs) {
            this.graphs = [];
        }
        switch(shape) {
            case 'Bar':
                this.graphs.push(new BarViz(height, width, margin, this.graphs.length));
                break;
            default:
                break;
        }
    },
    update: function(data) {
        for(var index in this.graphs) {
            this.graphs[index].update(data);
        }
    },
    done: function() {
        // Call this when the visualization is complete
        for(var index in this.graphs) {
            this.graphs[index].done();
        }
    },
    // Hide other graphs and show this one
    show: function(id) {
        // Do something
    }
}
