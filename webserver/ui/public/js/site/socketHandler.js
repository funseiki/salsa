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
            console.log(results);

            // Split by line breaks and make sure no elements are
            var data = results.data.split("\n")
                .filter(function(value) { return value.length > 0; });

            switch(that.state) {
                case 'SUM':
                case 'AVERAGE':
                    visualizer.update(data);
                    visualizer.done();
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
        });

        socket.on('snapshot', function(results) {
            visualizer.update(results.data);
        });
    },
    query: function(query) {
        this.socket.emit('query', query);
        this.state = query.type;
    },
    uiListeners: function() {
        // Method for setting up listeners to the UI
    }
};
