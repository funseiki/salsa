/************************************
 * Test socket server.
 * Placedholder for the Java daemon process
 ************************************/

var net = require('net'),
    async = require('async');

var server = net.createServer(function(socket) {
    console.log('Client connection received');
});

var port = process.argv[2] ? process.argv[2] : '8081';

server.listen(port, function() {
    var address = server.address();
    console.log("Opened a server on %j", address);
});

/************** TEST DATA ********************/
var tuples = [
    "A,1,2",
    "B,4,7",
    "C,2,13",
    "D,8,2",
    "D,11,2",
];

var attributes = ['Letter', 'Number1', 'Number2'];

/************* END TEST DATA ****************/

function sendResult(result, isFinal, socket) {
    var start = 'START_RESULT',
        end = 'END_RESULT';
    if(!isFinal) {
        start = 'START_SNAPSHOT';
        end = 'END_SNAPSHOT';
    }

    async.waterfall([
        function(cb) {
            socket.write(start+"\n", cb);
        },
        function(cb) {
            async.each(result, function(row, callback) {
                socket.write(row+"\n", callback);
            },
            function(err){
                if(err) { console.log(err); }
                cb();
            });
        }
    ], function(err) {
        socket.write(end+"\n");
    });
}

function doSum(column, groupBy, socket) {
    for(var i = 1; i < 6; i++) {
        var out = [
            "1," + i + "," + "4," + (5/(i)),
            "2," + (i*i) + "," + "4," + (5/(i)),
            "3," + (i+i) + "," + "4," + (5/(i)),
        ];
        if(i < 5) {
            setTimeout(sendResult, 3000, out, false, socket);
        }
        else {
            setTimeout(sendResult, 3000, out, true, socket);
        }
    }
}

function doTuples(socket) {
    sendResult(tuples, true, socket);
}

function doAttributes(socket) {
    sendResult(attributes, true, socket);
}

function parseInput(input, socket) {
    var inputString = input.toString();
    console.log(inputString);
    var params = inputString.split(" ");
    switch(params[0]) {
        case "SUM":
            doSum(params[1], params[2], socket);
            break;
        case "ATTRIBUTE_LIST":
            doAttributes(socket);
            break;
        case "TUPLES":
            doTuples(socket);
            break;
        default:
            break;
    }
}

server.on('connection', function(socket) {
    socket.on('data', function(data) {
        console.log('Server received: ', data.toString());
        parseInput(data, socket);
    });

    socket.on('end', function() {
        console.log('Client is disconnecting');
    });

    socket.on('error', function(error) {
        console.log(error);
    });
});

server.on('error', function(error) {
    console.log(error);
});
