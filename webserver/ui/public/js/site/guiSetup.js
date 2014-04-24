// Helper method for getting attributes
socket.on("attributes",function(data) {
    
});

function getAttributes(callback) {
    // we're getting attributes....
    // TODO: Use socket.io or a GET request to asynchronously grab attributes
    var attributes = ['one', 'two', 'three'];
    callback(attributes);
}

var attributeCallback = function(attributes) {
    console.log("these are the attributes:", attributes);

    for(var i = 0; i < attributes.length; i++) {
        // Do something here with the attributes
    }
};

getAttributes(attributeCallback);
