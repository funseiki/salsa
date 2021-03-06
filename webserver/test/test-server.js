/************************************
 * Test socket server.
 * Placedholder for the Java daemon process
 ************************************/

var net = require('net'),
    async = require('async'),
    path = require('path'),
    test_dump = require(path.join(__dirname,'data_poop'));

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

var attributes = ["Letter,Number1,Number2,Letter,Number1,Number2,Letter,Number1,Number2,Letter,Number1,Number2,Letter,Number1,Number2"];

/************* END TEST DATA ****************/

function startSend(socket, cb) {
    console.log("Writing to socket ", "START_RESULT");
    socket.write("START_RESULT\n", cb);
}
function endSend(socket, cb) {
    console.log("Writing to socket ", "END_RESULT");
    socket.write("END_RESULT\n", cb);
}

function sendResult(result, isFinal, socket, callback) {
    var start = 'START_RESULT',
        end = 'END_RESULT';
    if(!isFinal) {
        start = 'START_SNAPSHOT';
        end = 'END_SNAPSHOT';
    }

    async.waterfall([
        function(cb) {
            console.log("Writing to socket ", start);
            socket.write(start+"\n", cb);
        },
        function(cb) {
            async.each(result, function(row, callback) {
                console.log("Writing to socket ", row);
                socket.write(row+"\n", callback);
            },
            function(err){
                if(err) { console.log(err); }
                cb();
            });
        }
    ], function(err) {
        console.log("Writing to socket ", end);
        socket.write(end+"\n");
        callback();
    });
}

function doSum(column, groupBy, socket) {
    var i = 1;
    startSend(socket, stuff);
    function stuff() {
        var out = [
            "1," + i + "," + "4," + (5/(i)),
            "2," + (i*i) + "," + "4," + (5/(i)),
            "3," + (i+i) + "," + "4," + (5/(i)),
        ];
        if(i < 5) {
            sendResult(out, false, socket, function() {
                i++;
                setTimeout(stuff, 3000);
            });
        }
        else {
            sendResult(out, false, socket, function() {
                endSend(socket, function() {});
            });
        }
    }
}

function doTuples(socket) {
    sendResult(tuples, true, socket, function() {});
}

function doAverage(column, groupBy, socket) {
    var i = 0;
    startSend(socket, stuff);
    function stuff() {
        var out = test_dump[i];
        if(i < (test_dump.length - 1)) {
            sendResult(out, false, socket, function() {
                i++;
                setTimeout(stuff, 1000);
            });
        }
        else {
            sendResult(out, false, socket, function() {
                endSend(socket, function() {});
            });
        }
    }
}

function doAttributes(socket) {
    sendResult(attributes, true, socket, function() {});
}

function parseInput(input, socket) {
    var inputString = input.toString();
    console.log(inputString);
    var params = inputString.split("\n")[0].split(" ");
    console.log(params);
    switch(params[0]) {
        case "SUM":
            doSum(params[1], params[2], socket);
            break;
        case "AVERAGE":
            console.log('poopy');
            doAverage(params[1], params[2], socket);
            break;
        case "ATTRIBUTE_LIST":
            console.log('poopy');
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
