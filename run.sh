#!/bin/bash

HADOOP_HOME=$1

echo "Using path $HADOOP_HOME/commons-math3-3.2.jar"

cd mapreduce
if [ ! -d "mapreduce_classes" ]; then
	mkdir "mapreduce_classes"
fi

javac -cp $HADOOP_HOME/hadoop-hop-0.2-core.jar:$HADOOP_HOME/commons-math3-3.2.jar:$HADOOP_HOME/lib/commons-logging-1.0.4.jar:. -d mapreduce_classes Sum.java Average.java JobHandler.java

jar -cvf mapreduce.jar -C mapreduce_classes/ .

cd ../socket_daemon

javac -cp $HADOOP_HOME/hadoop-hop-0.2-core.jar:$HADOOP_HOME/lib/commons-logging-1.0.4.jar:$HADOOP_HOME/salsa/mapreduce/mapreduce.jar:. QueryDaemon.java QueryProtocol.java

echo "\nStarting the server..." 

java -cp ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:${HADOOP_HOME}/lib/commons-logging-1.0.4.jar:${HADOOP_HOME}/salsa/mapreduce/mapreduce.jar:. QueryDaemon 8081
