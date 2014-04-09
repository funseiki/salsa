var express = require('express')
    , http = require('http')
    , path = require('path');

var app = express();

// Configuration stuff
app.set('port', '8080');
app.use(express.static(path.join(__dirname, "ui/public")));

// Send index.html on '/' requests
app.get("/", function(req, res) {
    res.send("hello world");
    //res.sendfile(path.join(__dirname, "ui/public/index.html"));
});

http.createServer(app).listen(app.get('port'), function() {
    console.log("Express server listening on", app.get('port'));
});
