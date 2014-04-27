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
            switch(that.state) {
                case 'SUM':
                case 'AVERAGE':
                    visualizer.update(results.data);
                    visualizer.done();
                    break;
                case 'ATTRIBUTE_LIST':
                    guiBuilder.buildAttributeList(results.data);
                    break;
                case 'TUPLES':
                    guiBuilder.buildTupleList(results.data);
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
