var uiListener = {
    query: null,
    params: {groupBy: null, column: null},
    isSet: function(param) {
        return (this.params[param] != null && this.params[param] != undefined);
    },
    attributeSelected: function(target) {
        if(target.hasClass("groupBy-selected")) {
            return 'groupBy-selected';
        }
        else if (target.hasClass("column-selected")) {
            return "column-selected";
        }
        else {
            return null;
        }
    },
    parameterFromClassName: function(className) {
        if(className == "groupBy-selected") {
            return "groupBy";
        }
        else {
            return "column";
        }
    },
    classNameFromParameter: function(parameter) {
        if(parameter == 'groupBy') {
            return 'groupBy-selected';
        }
        else {
            return "column-selected";
        }
    },
    selectAttribute: function(target, parameter) {
        if(this.attributeSelected(target)) {
            // Deselect an attribute first
            return;
        }
        this.deselectAttributes(parameter);
        var className = this.classNameFromParameter(parameter);

        // Select this attribute
        target.addClass(className);

        // Set the attribute column number
        this.params[parameter] = {
            name: target.html(),
            columnNumber: target.data('column')
        };
        $('#' + parameter).html(this.params[parameter].name);
    },
    deselectAttributes: function(parameter) {
        $('.attribute').removeClass(this.classNameFromParameter(parameter));
        this.params[parameter] = null;
    },
    deselectAttribute: function(target) {
        var className = this.attributeSelected(target);
        if(!className) {
            return;
        }

        // Deselect any other attributes that may have been selected
        $('.attribute').removeClass(className);
        this.params[this.parameterFromClassName(className)] = null;
        $('#' + this.parameterFromClassName(className)).html("");
        return className;
    },
    eventListeners: function() {
        var that = this;
        $('.attribute').click(function(event) {
            if(event.shiftKey) {
                that.deselectAttribute($(event.target));
            }
            else {
                if(that.isSet('column')) {
                    that.selectAttribute($(event.target), 'groupBy');
                    $("span.groupBy-selected").text(that.params['groupBy'].name);
                }
                else {
                    that.selectAttribute($(event.target), 'column');
                    $("span.column-selected").text(that.params['column'].name);
                }
            }
        });

        $('.queryButton').click(function(event) {
            that.query = $(event.target).data('type');
            $('.queryButton').removeClass('btn-primary');
            $(event.target).addClass('btn-primary');
            $('#query').html(that.query);
            if(that.params.column)
            {
                var queryParams = {type: that.query, column: that.params.column.columnNumber};
                if(that.params.groupBy) {
                    queryParams.groupBy = that.params.groupBy.columnNumber;
                }
                socketHandler.query(queryParams);
            }
        });
    }
}
