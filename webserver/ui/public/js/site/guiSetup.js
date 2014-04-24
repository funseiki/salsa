// Helper method for getting attributes
//Wrap everything in a function - guiSetup

function guiSetup(socket) {
    var attributeCallback = function(attributes) {
        console.log("these are the attributes:", attributes);
        var list = document.getElementById('attributes');
        for(var i = 0; i < attributes.length; i++) {
            var entry = document.createElement('a');
            entry.href="#";
            entry.className = "list-group-item";
            entry.appendChild(document.createTextNode(attributes[i]));
            list.appendChild(entry);
        }
    };

    // Get the attributes
    socket.on("attributes", attributeCallback);
}
