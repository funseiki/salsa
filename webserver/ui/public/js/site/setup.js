function setupSocket(url, callbacks) {
    var socket = io.connect(url);
    socket.on('news', function(data) {
        console.log(data);
        socket.emit('pokemon', {pokemon: 'bulbasaur'});
    });
}

function setupViz() {
    var data = [1, 2, 3, 4, 10, 12, 40, 3, 5, 1];
    var width = 420,
        barHeight = 20;
    var viz = d3.select(".visualization");
    var bar = viz.selectAll("div")
                .data(data);
    var barEnter = bar.enter().append("div")
                    .style("width", function(d) { return d * 10 + "px"; })
                    .text(function(d) { return d; });
}

$(document).ready(function() {
    $.get('/serverinfo', function(data, status, xhr) {
        var url = data.ip + ":" + data.port;
        setupSocket(url, []);
        setupViz();
    });
});
