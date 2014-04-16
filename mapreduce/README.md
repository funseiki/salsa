Instructions to compile and run Average:

 ```javac -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar -d . Average.java``` <br>
 ```java -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:${HADOOP_HOME}/lib/commons-logging-1.0.4.jar:. mapreduce.Average <input> <output> <group_by_column> <average_column>```

Instructions to compile and run JobHandler:

 ```javac -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar -d . JobHandler.java``` <br>
 ```java -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:${HADOOP_HOME}/lib/commons-logging-1.0.4.jar:. mapreduce.JobHandler```

NOTE: Change the port of the Sum, Average and JobHandler to the ports that are used for the jobtracker, namenode when running hadoop online. <br>
Instructions to build mapreduce.jar which contains Sum, Average, JobHandler code needed for the QueryDaemon
 
  ```javac -cp ${HADOOP_HOME}/hadoop-hop-0.2-core.jar -d mapreduce_classes CustomInputFormat.java Sum.java Average.java JobHandler.java```
  ````jar -cvf mapreduce.jar -C mapreduce_classes/ .````
  
To test just the JobHandler use it as follows:
```java -cp $HADOOP_HOME/hadoop-hop-0.2-core.jar:$HADOOP_HOME/lib/commons-logging-1.0.4.jar:mapreduce.jar:. mapreduce.JobHandler sum 5 3``` <br>
```java -cp $HADOOP_HOME/hadoop-hop-0.2-core.jar:$HADOOP_HOME/lib/commons-logging-1.0.4.jar:mapreduce.jar:. mapreduce.JobHandler average 5 3```

To test JobHandler with the QueryDaemon check the socket daemon instructions.
