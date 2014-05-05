var margin = {top: 20, right: 30, bottom: 30, left: 100}
    width = 700 - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom;


// Singleton which will handle visualizing things
var visualizer = {
    clear: function() {
        Viz.globalClear();
        // TODO: clear the visualization
    },
    addGraph: function(shape, params) {
        if(!this.graphs) {
            this.graphs = [];
        }
        switch(shape) {
            case 'Bar':
                console.log(params);
                this.graphs.push(new BarViz(height, width, margin, params));
                break;
            case 'Line':
                this.graphs.push(new LineViz(height, width, margin, params.id, params.groupBy));
                break;
            default:
                break;
        }
    },
    update: function(data) {
        if(!data) {
            data = [];
        }
        if(this.graphs.length > 0) {
            Viz.globalUpdate(data);
        }
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
