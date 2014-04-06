Instructions to compile and run Average:

 javac -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar -d . Average.java
 java -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:${HADOOP_HOME}/lib/commons-logging-1.0.4.jar:. mapreduce.Average "input" "output" "group_by_column" "average_column"

Instructions to compile and run JobHandler:

 javac -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar -d . JobHandler.java
 java -classpath ${HADOOP_HOME}/hadoop-hop-0.2-core.jar:${HADOOP_HOME}/lib/commons-logging-1.0.4.jar:. mapreduce.JobHandler
