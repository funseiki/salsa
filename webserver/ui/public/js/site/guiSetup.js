// Singleton for building portions of the UI
var guiBuilder = {
    buildAttributeList: function(attributeArray) {
        var attributes = attributeArray[0].split(",")
            .filter(function(value) { return value.length > 0; });
        console.log("these are the attributes:", attributes);
        var list = $('#attributes');
        for(var i = 0; i < attributes.length; i++) {
            var entry = $('<a ></a>');
            entry.addClass("button list-group-item attribute")
            entry.attr('data-column', i);
            entry.append(attributes[i])
            list.append(entry);
        }
        uiListener.eventListeners();
    },

    buildTupleList: function(tuples) {
        console.log("these are the attributes:", attributes);
    }
};
