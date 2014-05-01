Instructions to compile and run QueryDaemon, ReadSnapshot, QuerySumMapReduce:
  1. You need to build the mapreduce file into a jar file called mapreduce.jar first. <br>
   ``javac -cp ../mapreduce/mapreduce.jar:/hadoop/hadoop-hop-0.2/hadoop-hop-0.2-core.jar QueryProtocol.java QueryDaemon.java ``
<br>
Instructions to run the QueryDaemon

```java -cp /hadoop/hadoop-hop-0.2/hadoop-hop-0.2-core.jar:../mapreduce/mapreduce.jar:/hadoop/hadoop-hop-0.2/lib/commons-logging-1.0.4.jar:. QueryDaemon 8081``` <br>
Instructions to compile and run the TestClient:

```javac TestClient.java ``` 

```java TestClient <port number that the QueryDaemon uses>``` 

    Server: Ready to process query.
    average 5 3 
    Client: average 5 3 
    Server: 46748392,0.0299 
  
