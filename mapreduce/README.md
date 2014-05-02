NOTE: Change the port and path of the Sum, Average and JobHandler to the ports that are used for the jobtracker, namenode when running hadoop online for your configuration in the Sum, Average, and JobHandler files.
<br>
OR Can change the hadoop-site.xml to use the port number 9000 for fs.default.name.
```
            conf.set("fs.defaultFS", "hdfs://localhost:9000/");
            conf.set("fs.default.name", "hdfs://localhost:9000");
            conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-site.xml"));
            conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-default.xml")); ```
<br>


Instructions to build mapreduce.jar which contains Sum, Average, JobHandler code needed for the QueryDaemon in the map_reduce
 
  ```javac -cp /hadoop/hadoop-hop-0.2/hadoop-hop-0.2-core.jar:/library/commons-math3-3.2.jar -d mapreduce_classes Sum.java Average.java JobHandler.java ``` <br>
  ````jar -cvf mapreduce.jar -C mapreduce_classes/ .````
  
