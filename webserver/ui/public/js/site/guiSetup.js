// Singleton for building portions of the UI
var guiBuilder = {
    buildAttributeList: function(attributeArray) {
        var attributes = attributeArray[0].split(",")
            .filter(function(value) { return value.length > 0; });
        console.log("these are the attributes:", attributes);
        var list = $('#attributes');
        var row = $('<tr></tr>');
        for(var i = 0; i < attributes.length; i++) {
            var entry = $('<a ></a>');
            entry.addClass("button list-group-item attribute")
            entry.attr('data-column', i);
            entry.append(attributes[i])
            list.append(entry);
            var column = $('<td>'+attributes[i]+'</td>');
            row.append(column);
        }
        $('.table').append(row);
        uiListener.eventListeners();
    },

    buildTupleList: function(tuples) {
        console.log("these are the tuples:", tuples);
    },

    addTab: function(tabname){
        var tab = $('.nav-tabs')
        var newDiv = $('<div></div>');
        var close = '<button class="close closeTab" id="#'+tabname+'button" type="button" >×</button>';
        newDiv.addClass("tab-pane active");
        newDiv.attr("id",tabname);
        newDiv.append("<p>Loading new tab..</p>")
        $(".tab-content").append(newDiv);
        $('.nav-tabs').append('<li><a href=#'+tabname+' data-toggle="tab">'+close+
            tabname+'&nbsp</a></li>');
        $('.nav-tabs a:last').tab('show');
    },

    deleteTab: function(tab){
        var tabContentId = tab.parent().attr("href");
        tab.parent().parent().remove(); //remove li of tab
        $('.nav-tabs a:last').tab('show'); // Select first tab
        $(tabContentId).remove();
    }

};
