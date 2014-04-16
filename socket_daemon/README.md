Instructions to compile and run QueryDaemon, ReadSnapshot, QuerySumMapReduce:
  1. You need to build the mapreduce file into a jar file called mapreduce.jar first.

   ```javac -cp ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:mapreduce.jar *.java ``` <br>

Instructions to run the QueryDaemon

```java -cp ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:/{$HADOOP_HOME}/lib/commons-logging-1.0.4.jar:mapreduce.jar:. QueryDaemon <portnumber> ``` <br>

Instructions to compile and run the TestClient:

```javac TestClient.java ``` 

```java TestClient <port number that the QueryDaemon uses>``` 

    Server: Ready to process query.
    average 5 3 
    Client: average 5 3 
    Server: 46748392,0.0299 
  
