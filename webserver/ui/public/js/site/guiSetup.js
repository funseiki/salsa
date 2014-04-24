// Helper method for getting attributes
//Wrap everything in a function - guiSetup

function guiSetup(socket) {
    var attributeCallback = function(attributes) {
        console.log("these are the attributes:", attributes);

        for(var i = 0; i < attributes.length; i++) {
            // Do something here with the attributes
        }
    };

    // Get the attributes
    socket.on("attributes", attributeCallback);
}
