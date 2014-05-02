Instructions to compile and run QueryDaemon, ReadSnapshot, QuerySumMapReduce:
  1. You need to build the mapreduce file into a jar file called mapreduce.jar first. <br>
   ``javac -cp ../mapreduce/mapreduce.jar:/hadoop/hadoop-hop-0.2/hadoop-hop-0.2-core.jar QueryProtocol.java QueryDaemon.java ``
<br>

Instructions to run the QueryDaemon:
  1. First, place the yahoo_data part-m-00000 or part-r-00000 uncompressed in the /yahoo_data directory on the HDFS. <br>
  2. Download the apache commons math 3.2 library jar from here : http://commons.apache.org/proper/commons-math/download_math.cgi <br>
  3. Add the apache commons math jar path to hadoop-env.sh to the following lines: <br>
    ```export HADOOP_CLASSPATH=/lib/commons-math3-3.2/commons-math3-3.2.jar```
    ```HADOOP_TASKTRACKER_OPTS="-classpath<colon-separated-paths-to-your-jars>"``` <br>
  4. Restart Hadoop for the classpath changes to take effect. <br>
  
```java -cp /hadoop/hadoop-hop-0.2/hadoop-hop-0.2-core.jar:../mapreduce/mapreduce.jar:/hadoop/hadoop-hop-0.2/lib/commons-logging-1.0.4.jar:. QueryDaemon 8081``` <br>

Instructions to compile and run the TestClient: <br>
  
```javac TestClient.java ``` <br>

```java TestClient <port number that the QueryDaemon uses>``` 

    Server: Ready to process query.
    average 5 3 
    Client: average 5 3 
    Server: 46748392,0.0299 
  
