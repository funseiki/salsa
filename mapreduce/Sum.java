
/* 
CS 511
Spring 2014
Real time Query Results Project  
Map Reduce program for sum queries
*/

package mapreduce;

// needed to calculate the confidence interval
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.*;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
 
import java.io.IOException;  
import java.util.*;  
import java.io.*;
import java.net.*;
 
import org.apache.hadoop.fs.Path;  
import org.apache.hadoop.conf.*;  
import org.apache.hadoop.io.*;  
import org.apache.hadoop.mapred.*;  
import org.apache.hadoop.util.*;  

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.filecache.DistributedCache;
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

class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {   
       private Text word = new Text();  

       public int column;
       public int group_by;

      public void configure(JobConf job) {
         group_by = job.getInt("group_by",-1);
         column = job.getInt("column",-1);
       }

       public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {   
           String line = value.toString(); 
           String index = key.toString();

           String delims = "[,]";
           String[] tokens = line.split(delims);
           System.out.println("Sum on column " + column + " groupby " + group_by);
	   //word.set(tokens[5].trim());
           if(group_by != -1)
              word.set(tokens[group_by].trim());
           else
              word.set("");
           /*DoubleWritable  value_sum = new DoubleWritable();
           if(column != -1)
               value_sum.set(Integer.parseInt(tokens[column]));   
           else
               value_sum.set(1);
           */
           Text val = new Text(); 
           if(column != -1)
              val.set(tokens[column].trim());
           else
              val.set("0");
           output.collect(word, val);  
      }  
}

/* Map Reduce for the yahoo campaign_id
*/ 
public class Sum{  
  
     public static void main(String[] args) throws Exception {   
         Sum test = new Sum();
         test.run(args[0], args[1], args[2], args[3]);
     }

     RunningJob rjob;
     public void stopJob() throws Exception
     {
         rjob.killJob();
     }

     public void run(String inputPath, String outputPath, String key_index, String value_index) throws Exception {      
        System.out.println("Group by " + key_index + " sum column " + value_index);

        JobConf conf = new JobConf(Sum.class);   
        conf.set("mapred.job.tracker", "localhost:9001");
        //conf.set("fs.default.name", "hdfs://localhost:54310");
        conf.set("fs.default.name", "hdfs://localhost:9000");
        conf.setJobName("Sum");  

        int group_by = Integer.parseInt(key_index);
        conf.setInt("group_by",group_by);
        int column = Integer.parseInt(value_index);
        conf.setInt("column",column);

        conf.setOutputKeyClass(Text.class);  
        conf.setOutputValueClass(Text.class);  

        // Select the map class which belongs to the certain column
        conf.setMapperClass(Map.class);  

        //conf.setCombinerClass(ConfReduce.class);  
        conf.setReducerClass(ConfReduce.class);  
 
        conf.setInputFormat(TextInputFormat.class);  

        conf.set("mapred.textoutputformat.separator", ",");
        conf.setOutputFormat(TextOutputFormat.class); 
 
        // To read from a compressed file
        conf.set("mapreduce.job.inputformat.class", "com.wizecommerce.utils.mapred.TextInputFormat");
 
        //FileInputFormat.setInputPaths(conf, new Path(inputPath));  
        Path out = new Path(outputPath);
        FileSystem fs = FileSystem.get(conf);
        fs.delete(out, true);
        
        //DistributedCache.addFileToClassPath(new Path("/lib/commons-math3-3.2.jar"), conf); //, fs);
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));  

         FileInputFormat.addInputPath(conf, new Path(inputPath));
         
         conf.setFloat("mapred.snapshot.frequency", Float.parseFloat("0.10"));
         conf.setBoolean("mapred.map.pipeline", true);
         JobClient client = new JobClient(conf);
         //client.submitJob(conf);
          rjob = client.runJob(conf);

         /* 
         RunningJob rjob = client.submitJob(conf);
         while(rjob.isComplete() != true)
         {

         } */
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
        System.out.println("key " + key + " sum: " + sum);
      }  
    }  

     public static class ConfReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {   
       public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {   
       double sum = 0;  
       int num_vals = 0;
        
       SummaryStatistics stats = new SummaryStatistics();
        while (values.hasNext()) { 
          String[] str = values.next().toString().split(",");; 
          double val = Double.parseDouble(str[0]);
          //sum = sum + values.next().get();
          sum = sum + val;
          num_vals++;
          stats.addValue(val);
          System.out.println("n= " + num_vals + " val= " + val);
        }  
       
        //Calculate 95% confidence interval
        double ConfInter;
        if(num_vals > 1)
          ConfInter = calcMeanCI(stats, 0.95);
        else
          ConfInter = 0;
        ConfInter = ConfInter * num_vals;
        StringBuilder outpt = new StringBuilder();
        outpt.append(sum);
        outpt.append(",");
        outpt.append(num_vals);
        outpt.append(",");
        outpt.append(ConfInter);
        Text out = new Text();
        out.set(outpt.toString());
        System.out.println("key: " + key + " sum: " + sum + " conf:" + ConfInter);
        output.collect(key, out);
      }  
 
      public double calcMeanCI(SummaryStatistics stats, double level)
      {
    //      try {
     {       // Create T Distribution with N-1 degrees of freedom
            TDistribution tDist = new TDistribution(stats.getN() - 1);
            // Calculate critical value
            double critVal = tDist.inverseCumulativeProbability(1.0 - (1 - level) / 2);
            // Calculate confidence interval
            return critVal * stats.getStandardDeviation() / Math.sqrt(stats.getN());
  //            } catch (OutOfRangeException e) {
 //MathIllegalArgumentException e) {
                  // System.out.println("Exception in calcMean " + e);
                   // return Double.NaN;
            }
       }
    } // end Reduce class  
  
  
 }  
 
