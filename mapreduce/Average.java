package mapreduce;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

// needed to calculate the confidence interval
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class Average {


public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    private FloatWritable outputValue = new FloatWritable();
    private Text outputKey = new Text();

    public int column;
    public int group_by;

    public void configure(JobConf job) {
         group_by = job.getInt("group_by",-1);
         column = job.getInt("column",-1);
         
    }
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        System.out.println(key);
        String line = value.toString();
        String[] fields = line.split(",");
        if(group_by != -1)
            outputKey.set(fields[group_by]);
        else
            outputKey.set("AVERAGE");
        /*if(column != -1)
            //outputValue.set(Float.parseFloat(fields[column]));
            outputValue.set(Float.parseFloat(fields[column]));
        else
            outputValue.set(0f);
        */
        Text val = new Text();
        if(column != -1)
           val.set(fields[column].trim());
        else 
           val.set("0");
        output.collect(outputKey, val);
        }
    }
 

 public static class Reduce extends MapReduceBase implements Reducer<Text, FloatWritable, Text, FloatWritable> {

    public void reduce(Text key, Iterator<FloatWritable> values, OutputCollector<Text, FloatWritable> output, Reporter reporter) throws
IOException {
        float value, count = 0f;
        float sum = 0;
        while(values.hasNext()) {
            value = values.next().get();
            sum+=value;
            count+=1;
        }
        float average = sum/count;
        output.collect(key, new FloatWritable(average));
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
        double average = sum / num_vals;
        StringBuilder outpt = new StringBuilder();
        outpt.append(average);
        outpt.append(",");
        //outpt.append(num_vals);
        //outpt.append(",");
        outpt.append(ConfInter);
        Text out = new Text();
        out.set(outpt.toString());
        System.out.println("key: " + key + " average: " + average + " conf:" + ConfInter);
        output.collect(key, out);

        // Write to HDFS directly
        
        /*Configuration confr = new Configuration();
        FileSystem fs = FileSystem.get(confr);
        FSDataOutputStream dos = fs.create(new Path("/tmp/tmp"), true); 
        dos.writeChars(key.toString());
        dos.writeChars(",");
        dos.writeChars(outpt.toString());
        dos.writeChars("\n");
        dos.close(); */
        
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
 

    RunningJob rjob;
    public void stopJob() throws Exception
    {
        System.out.println("CANCELLING JOB");
            //while(rjob.setupProgress() == JobStatus.PREP )
              //  continue;

                //System.out.println("Killing job ........ " + rjob.getJobName());
        System.out.println("JOB prep is done, so can cancel job");
           rjob.killJob();
        System.out.println("DID KILL JOB");
    }

    public String getJobId()
    {
       return rjob.getID().toString();
    }

    public void run(String inputPath, String outputPath, String key_index, String value_index) throws Exception
    {
        /* Set column based on command line argument*/
        JobConf conf = new JobConf(Average.class);
        //conf.set("mapred.job.tracker", "localhost:50030");
        conf.set("mapred.job.tracker", "localhost:9001");
        //conf.set("fs.default.name", "hdfs://localhost:54310");
        conf.set("fs.default.name", "hdfs://localhost:9000");
        conf.setJobName("average");

        int group_by = Integer.parseInt(key_index);
        conf.setInt("group_by",group_by);
        int column = Integer.parseInt(value_index);
        conf.setInt("column",column);

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapperClass(Map.class);
        conf.setReducerClass(ConfReduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.set("mapred.textoutputformat.separator",",");
        conf.setOutputFormat(TextOutputFormat.class);

        //FileInputFormat.setInputPaths(conf, new Path(inputPath));
        //FileOutputFormat.setOutputPath(conf, new Path(outputPath));

        conf.setFloat("mapred.snapshot.frequency", Float.parseFloat("0.1"));
        conf.setBoolean("mapred.map.pipeline", true);
        //Average.column = 2;

        FileInputFormat.addInputPath(conf, new Path(inputPath));
        Path out = new Path(outputPath);
        FileSystem fs = FileSystem.get(conf);
        fs.delete(out, true);
        FileOutputFormat.setOutputPath(conf, out);

        String jobID = conf.get("mapred.job.id");
        JobClient jobClient = new JobClient(conf);
        rjob = jobClient.getJob(JobID.forName(jobID));

        rjob = jobClient.submitJob(conf);
         rjob.waitForCompletion();
    }
        
 public static void main(String[] args) throws Exception {

    Average job = new Average();
    job.run(args[0], args[1], args[2], args[3]);
	
 }
}
