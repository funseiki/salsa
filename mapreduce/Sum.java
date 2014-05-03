
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

// needed to calculate the input size
import java.lang.Runtime;
 
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

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
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
              word.set("SUM");

           StringBuilder outpt = new StringBuilder();
           Text val = new Text(); 
           if(column != -1)
              outpt.append(tokens[column].trim());
           else
             outpt.append("0");
           outpt.append(",");
           outpt.append(line.length()+2);
           val.set(outpt.toString());
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
         System.out.println("Cancelling Sum job");
         rjob.killJob();
         System.out.println("Killed the sum job");
     }

     long getInputSize(JobConf conf,FileSystem fs, String inputPath)
     {
         Path inpath = new Path(inputPath);
         long total_size = 0;
         try{

         if(fs.isDirectory(inpath))
         {
            System.out.println("Input path " + inputPath + " is a directory");
            FileStatus[] status = fs.listStatus(inpath);
            for(int i=0;i<status.length;i++)
            {
              Path path = status[i].getPath();
              String fileName = path.getName();
              System.out.println("File in path " + fileName);
              if(fileName.contains("part"))
              {
                  /* if(fileName.contains(".gz"))
                   {
                       System.out.println("File is gz");
                       String cmd = "\"/hadoop/hadoop-hop-0.2/bin/hadoop dfs -cat " + inputPath + "/" + fileName + " | gzcat | wc -c\" ";

                       String[] command = new String[3];
                       command[0] = "/bin/sh";
                       //command[1] = "-c";
                       command[1] = " ";
                       command[2] = cmd;
                       System.out.println("Cmd: " + cmd);
                       System.out.println("Command: " + command[0] + "===" + command[1] + " ====" + command[2]);
                       Runtime run = Runtime.getRuntime();
                       Process p = null;
                       try{
                          p = run.exec(command);
                          System.out.println("Command: " + command);
                          InputStream stderr = p.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            System.out.println("<ERROR>");
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            System.out.println("</ERROR>");
                       }catch(IOException e)
                       {
                          System.err.println("Error in run in gzip command" + e);
                       }
                       try{
                          System.out.println("Waiting for command to complete");
                          p.waitFor();
                          System.out.println("Waiting for command to complete");
                       } catch(InterruptedException e) {

                           System.err.println("Error in waiting for process of gzip command " + e);
                       }
                       BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream())); 
                       String line = ""; 
                       try {
                          while ((line=buf.readLine())!=null)
                          {
                             String[] token = line.split(" ");
                             long val = Long.parseLong(token[2]);
                             total_size += val;
                             System.out.println(line);
                          }
                      } catch (IOException e) { e.printStackTrace(); }  
                   } //if file is .gz
                   else */
                   { 
                      total_size = total_size + status[i].getLen();
                   }
              } // if file is part

           } // for all files in input dire
        } // if input path is directory
        else
        {
           FileStatus stat = fs.getFileStatus(inpath);
           total_size = stat.getLen(); 
        }
        
        } catch(Exception e) { // try end for is input path a directory
             System.err.println("Exception in check input path directory " + e);
        }
        return total_size;
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

        long input_size = getInputSize(conf, fs, inputPath);
        System.out.println("########### INPUT SIZE " + input_size + " ##########");
        conf.setLong("input_size", input_size);
        
        //DistributedCache.addFileToClassPath(new Path("/lib/commons-math3-3.2.jar"), conf); //, fs);
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));  

         FileInputFormat.addInputPath(conf, new Path(inputPath));
         
         conf.setFloat("mapred.snapshot.frequency", Float.parseFloat("0.10"));
         conf.setBoolean("mapred.map.pipeline", true);
         JobClient client = new JobClient(conf);
         rjob = client.submitJob(conf);
         rjob.waitForCompletion();

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

       public long input_size;
       public void configure(JobConf job)
       {
            input_size = job.getLong("input_size" , 0);         
       }
       public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {   
       double sum = 0;  
       int num_vals = 0;
       double total_bytes = 0; 
       double bytes = 0;
       SummaryStatistics stats = new SummaryStatistics();
        while (values.hasNext()) { 
          String[] str = values.next().toString().split(",");; 
          double val = Double.parseDouble(str[0]);
          sum = sum + val;
          num_vals++;
          bytes = Double.parseDouble(str[1]);
          total_bytes += bytes; 
          stats.addValue(val);
          //System.out.println("n= " + num_vals + " val= " + val);
        }  
         double avg_bytes_of_tuple = total_bytes / num_vals; 
         System.out.println("n= " + num_vals + " num_bytes= " + avg_bytes_of_tuple); 
        double avg = sum / num_vals;
        double num_tuples = input_size / avg_bytes_of_tuple;
        double total_sum =  avg * num_tuples;
        System.out.println("avg " + avg + " input_size "+ input_size + " bytes " + bytes + "  num_tuples " + num_tuples);
        //Calculate 95% confidence interval
        double ConfInter;
        if(num_vals > 1)
          ConfInter = calcMeanCI(stats, 0.95);
        else
          ConfInter = 0;
        double sum_ConfInter;
        if(num_vals >= num_tuples)
           sum_ConfInter = 0;
        else
           sum_ConfInter = ConfInter * num_tuples;
        StringBuilder outpt = new StringBuilder();
        if(num_vals >= num_tuples)
          outpt.append(sum);
        else
          outpt.append(total_sum);
        outpt.append(",");
        //outpt.append(num_vals);
        //outpt.append(",");
        outpt.append(sum_ConfInter);
        Text out = new Text();
        out.set(outpt.toString());
        System.out.println("key: " + key + " sum: " + total_sum + " conf:" + sum_ConfInter);
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
 
