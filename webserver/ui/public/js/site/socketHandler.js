var socketHandler = {
    state: 'ATTRIBUTE_LIST',
    setupListeners: function(socket) {
        this.socket = socket;
        var that = this;
        this.query({type: 'ATTRIBUTE_LIST'});
        socket.on('error', function() {
            console.log("Error with the socket");
        });

        socket.on('client_error', function(message) {
            console.log(message);
        });

        socket.on('result', function(results) {
            console.log('result', results);

            // Split by line breaks and make sure no elements are
            var data = results.data.split("\n")
                .filter(function(value) { return value.length > 0; });

            switch(that.state) {
                case 'SUM':
                case 'AVERAGE':
                    visualizer.update();
                    visualizer.done();
                    $("#myprogress").css('width', "100%");
                    break;
                case 'ATTRIBUTE_LIST':
                    guiBuilder.buildAttributeList(data);
                    break;
                case 'TUPLES':
                    guiBuilder.buildTupleList(data);
                    break;
                default:
                    break;
            }
            that.state = "READY";
        });

        socket.on('snapshot', function(results) {
            console.log('snapshot', results['data']);
            var data = results['data'].split("\n")
                .filter(function(value) { return value.length > 0; });
            var perc = parseFloat(data[0].split(",")[3])*100;
            $("#myprogress").css('width', perc+"%");
            visualizer.update(data);

        });
    },
    getTuples: function() {
        this.query({type: 'TUPLES'});
    },
    cancelQuery: function(){
        this.query({type: 'CANCEL'});
        visualizer.done();
    },
    query: function(query) {
        if(query.type != 'ATTRIBUTE_LIST' && query.type != 'TUPLES' && query.type != 'CANCEL') {
            visualizer.clear();
            visualizer.done();
            var uid = guiBuilder.uid();
            $("#myprogress").css('width', "0%");
            guiBuilder.addTab(query.type);
            visualizer.addGraph('Bar', uid);
        }

        this.socket.emit('query', query);
        this.state = query.type;
    }
};
