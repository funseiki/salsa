// Utility method for extending classes
function extend(ChildConstructor, ParentConstructor) {
    ChildConstructor.prototype = new ParentConstructor();
    ChildConstructor.prototype.constructor = ChildConstructor;
}
