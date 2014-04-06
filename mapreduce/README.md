Instructions to compile and run Average:

 ```javac -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar -d . Average.java``` <br>
 ```java -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:${HADOOP_HOME}/lib/commons-logging-1.0.4.jar:. mapreduce.Average <input> <output> <group_by_column> <average_column>```

Instructions to compile and run JobHandler:

 ```javac -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar -d . JobHandler.java``` <br>
 ```java -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:${HADOOP_HOME}/lib/commons-logging-1.0.4.jar:. mapreduce.JobHandler```

Instructions to compile and run QueryDaemon, ReadSnapshot, QuerySumMapReduce:

javac -cp ${HADOOP_HOME}/hadoop-hop-0.2-core.jar -d query_classes *.java
jar -cvf query.jar -C query_classes/ .

Instrcuctions to run the QueryDaemon
hadoop jar query.jar QueryDaemon <port number>

Instructions to compile and run the TestClient:
javac TestClient.java
java TestClient <port number that the QueryDaemon uses>
      Server: Ready to process query.
      sum 5 3
      Server: 1099 99.0
      sum 2 4
      
