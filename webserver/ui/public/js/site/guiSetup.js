// Singleton for building portions of the UI
var guiBuilder = {
    buildAttributeList: function(data) {
        var attributes = data.split("\n");
        console.log("these are the attributes:", attributes);
        var list = document.getElementById('attributes');
        for(var i = 0; i < attributes.length; i++) {
            if(attributes[i].length > 0) {
                var entry = document.createElement('a');
                entry.href="#";
                entry.className = "list-group-item";
                entry.appendChild(document.createTextNode(attributes[i]));
                list.appendChild(entry);
            }
        }
    },

    buildTupleList: function(tuples) {
        console.log("these are the attributes:", attributes);
    }
};
