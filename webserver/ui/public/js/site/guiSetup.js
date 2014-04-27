// Singleton for building portions of the UI
var guiBuilder = {
    buildAttributeList: function(attributes) {
        console.log("these are the attributes:", attributes);
        var list = $('#attributes');
        for(var i = 0; i < attributes.length; i++) {
            var entry = $('<a ></a>');
            entry.addClass("button list-group-item")
            entry.attr("href","#");
            entry.append(attributes[i])
            list.append(entry);
        }
    },

    buildTupleList: function(tuples) {
        console.log("these are the attributes:", attributes);
    }
};
