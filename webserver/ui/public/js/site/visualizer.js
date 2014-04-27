// Singleton which will handle visualizing things
var visualizer = {
    clear: function() {
        // TODO: clear the visualization
    },
    startViz: function(shape, params) {
        // TODO: add support for more visualizations
        var data = [1, 2, 3, 4, 10, 12, 40, 3, 5, 1];
        var width = 420,
            barHeight = 20;
        var viz = d3.select(".visualization");
        var bar = viz.selectAll("div")
                    .data(data);
        var barEnter = bar.enter().append("div")
                        .style("width", function(d) { return d * 10 + "px"; })
                        .text(function(d) { return d; });
    },
    update: function(data) {
        // Call this when there is data to update the visualization with

    },
    done: function() {
        // Call this when the visualization is complete
    }
}
