function BarViz(height, width, margin, id) {
    Viz.apply(this, arguments);
}
extend(BarViz, Viz);

BarViz.prototype.update = function(inData){
    // Only update if we're not done yet
    if(this.done) {
        return;
    }

    inData = this.convert(inData);

    this.x = d3.scale.linear()
        .rangeRoundBands([0, this.width])
        // Dummy domain (we'll change this when we get the data)
        .domain([0, 100]);

    this.y = d3.scale.linear()
        // Map y(0) to height and y(max(data) to 0). Scale down linearly
        .range([this.height, 0])
        // Dummy domain (this'll change most probably)
        .domain([0, 100]);
};
