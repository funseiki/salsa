NOTE: Change the port of the Sum, Average and JobHandler to the ports that are used for the jobtracker, namenode when running hadoop online. <br>
Instructions to build mapreduce.jar which contains Sum, Average, JobHandler code needed for the QueryDaemon in the map_reduce
 
  ```javac -cp /hadoop/hadoop-hop-0.2/hadoop-hop-0.2-core.jar:/library/commons-math3-3.2.jar -d mapreduce_classes Sum.java Average.java JobHandler.java ``` <br>
  ````jar -cvf mapreduce.jar -C mapreduce_classes/ .````
  
