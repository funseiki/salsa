
/* 
CS 511
Spring 2014
Real time Query Results Project  
Map Reduce program for sum queries
*/

package mapreduce;
 
import java.io.IOException;  
import java.util.*;  
import java.io.*;
 
import org.apache.hadoop.fs.Path;  
import org.apache.hadoop.conf.*;  
import org.apache.hadoop.io.*;  
import org.apache.hadoop.mapred.*;  
import org.apache.hadoop.util.*;  

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Progressable;

import org.apache.hadoop.io.compress.CompressionCodecFactory;

class Map extends MapReduceBase implements Mapper<Text, Text, Text, FloatWritable> {   
       private Text word = new Text();  

       public void map(Text key, Text value, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws IOException {   
           String line = value.toString(); 
           String index = key.toString();

           String delims = "[,]";
           String[] indexes = index.split(delims);
           String[] tokens = line.split(delims);
	   //word.set(tokens[5].trim());
           int key_index = Integer.parseInt(indexes[0]);
           int value_index = Integer.parseInt(indexes[1]);
           if(key_index != -1)
              word.set(tokens[key_index].trim());
           else
              word.set("");
           FloatWritable value_sum = new FloatWritable();
           if(value_index != -1)
               value_sum.set(Integer.parseInt(tokens[value_index]));   
           else
               value_sum.set(1);
           output.collect(word, value_sum);  
      }  
}

/* Map Reduce for the yahoo campaign_id
*/ 
public class Sum{  
  
     public static void main(String[] args) throws Exception {   
         Sum test = new Sum();
         test.run(args[0], args[1], args[2], args[3]);
     }

     public void run(String inputPath, String outputPath, String key_index, String value_index) throws Exception {      
        String index = key_index + ","+value_index;
        System.out.println("Column indexes are " + index);

        JobConf conf = new JobConf(Sum.class);   
        conf.set("mapred.job.tracker", "localhost:9001");
        //conf.set("fs.default.name", "hdfs://localhost:54310");
        conf.set("fs.default.name", "hdfs://localhost:9000");
        conf.setJobName(index);  
        conf.setOutputKeyClass(Text.class);  
        conf.setOutputValueClass(FloatWritable.class);  

        // Select the map class which belongs to the certain column
        conf.setMapperClass(Map.class);  

        conf.setCombinerClass(Reduce.class);  
        conf.setReducerClass(Reduce.class);  
 
        conf.setInputFormat(CustomInputFormat.class);  

        conf.set("mapred.textoutputformat.separator", ", ");
        conf.setOutputFormat(TextOutputFormat.class); 
 
        // To read from a compressed file
        conf.set("mapreduce.job.inputformat.class", "com.wizecommerce.utils.mapred.TextInputFormat");
 
        //FileInputFormat.setInputPaths(conf, new Path(inputPath));  
        Path out = new Path(outputPath);
        FileSystem fs = FileSystem.get(conf);
        fs.delete(out, true);
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));  

         FileInputFormat.addInputPath(conf, new Path(inputPath));
         
         conf.setFloat("mapred.snapshot.frequency", Float.parseFloat("0.10"));
         conf.setBoolean("mapred.map.pipeline", true);
         JobClient client = new JobClient(conf);
         //client.submitJob(conf);
         client.runJob(conf);
         //RunningJob rjob = client.submitJob(conf);
         //client.report(rjob, conf);

         //JobID jobid = rjob.getID();
        // TaskReport[] trs = client.getReduceTaskReports(jobid);
  
        //JobClient.runJob(conf);  
     }

     public static class Reduce extends MapReduceBase implements Reducer<Text, FloatWritable, Text, FloatWritable> {   
       public void reduce(Text key, Iterator<FloatWritable> values, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws IOException {   
       float sum = 0;  
        while (values.hasNext()) {  
          sum = sum + values.next().get();
        }  
        output.collect(key, new FloatWritable(sum));  
      }  
    }  
  
 }  
 
