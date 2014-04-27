// Singleton for building portions of the UI
var guiBuilder = {
    buildAttributeList: function(attributes) {
        console.log("these are the attributes:", attributes);
        var list = document.getElementById('attributes');
        for(var i = 0; i < attributes.length; i++) {
            var entry = document.createElement('a');
            entry.href="#";
            entry.className = "list-group-item";
            entry.appendChild(document.createTextNode(attributes[i]));
            list.appendChild(entry);
        }
    },

    buildTupleList: function(tuples) {
        console.log("these are the attributes:", attributes);
    }
};
