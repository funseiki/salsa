package mapreduce;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class Average {


public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, FloatWritable> {
    private FloatWritable outputValue = new FloatWritable();
    private IntWritable outputKey = new IntWritable();

    public int column;

    public void configure(JobConf job) {
         column = job.getInt("column",0);
         outputKey.set(column);
    }
    public void map(LongWritable key, Text value, OutputCollector<IntWritable, FloatWritable> output, Reporter reporter) throws IOException {
        String line = value.toString();
        String[] fields = line.split(",");
        outputValue.set(Float.parseFloat(fields[column]));
        output.collect(outputKey, outputValue);
        }
    }
 

 public static class Reduce extends MapReduceBase implements Reducer<IntWritable, FloatWritable, IntWritable, FloatWritable> {

    public void reduce(IntWritable key, Iterator<FloatWritable> values, OutputCollector<IntWritable, FloatWritable> output, Reporter reporter) throws
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


    public void run(String inputPath, String outputPath, String key_index, String value_index) throws Exception
    {
        /* Set column based on command line argument*/
        JobConf conf = new JobConf(Average.class);
        conf.set("mapred.job.tracker", "localhost:54311");
        conf.set("fs.default.name", "hdfs://localhost:54310");
        conf.setJobName("average");
        int column = Integer.parseInt(value_index);
        conf.setInt("column",column);

        conf.setOutputKeyClass(IntWritable.class);
        conf.setOutputValueClass(FloatWritable.class);

        conf.setMapperClass(Map.class);
        conf.setReducerClass(Reduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.set("mapred.textoutputformat.separator",",");
        conf.setOutputFormat(TextOutputFormat.class);

        //FileInputFormat.setInputPaths(conf, new Path(inputPath));
        //FileOutputFormat.setOutputPath(conf, new Path(outputPath));

        conf.setFloat("mapred.snapshot.frequency", Float.parseFloat("0.10"));
        conf.setBoolean("mapred.map.pipeline", true);
        //Average.column = 2;

        FileInputFormat.addInputPath(conf, new Path(inputPath));
        Path out = new Path(outputPath);
        FileSystem fs = FileSystem.get(conf);
        fs.delete(out, true);
        FileOutputFormat.setOutputPath(conf, out);


        JobClient.runJob(conf);
    }
        
 public static void main(String[] args) throws Exception {

    Average job = new Average();
    job.run(args[0], args[1], args[2], args[3]);
	
 }
}
