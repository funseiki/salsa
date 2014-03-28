import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class Average {


public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, FloatWritable> {
    private FloatWritable outputValue = new FloatWritable();
    private IntWritable outputKey = new IntWritable();

    public int column;
    /*Configure column parameter*/
    public void configure(JobConf job) {
         column = job.getInt("column",0);
         outputKey.set(column);
    }

    /*Mapper passes output with only one key i.e. to only one reducer*/
    public void map(LongWritable key, Text value, OutputCollector<IntWritable, FloatWritable> output, Reporter reporter) throws IOException {
        String line = value.toString();
        String[] fields = line.split(",");
        outputValue.set(Float.parseFloat(fields[column]));
        output.collect(outputKey, outputValue);
        }
    }
 

 public static class Reduce extends MapReduceBase implements Reducer<IntWritable, FloatWritable, IntWritable, FloatWritable> {


    /*Loop through all values to get sum and count*/
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
        
 public static void main(String[] args) throws Exception {


	String input, output;
        JobConf conf = new JobConf(Average.class);
        conf.setJobName("average");
        /* Set column based on command line argument*/
	if(args.length > 2)
	{
		int column = Integer.parseInt(args[0]);
		conf.setInt("column",column);
		input = args[1];
		output = args[2];
	}
	else
	{
		input = args[0];
		output = args[1];
	}
        conf.setOutputKeyClass(IntWritable.class);
        conf.setOutputValueClass(FloatWritable.class);

        conf.setMapperClass(Map.class);
        conf.setReducerClass(Reduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.set("mapred.textoutputformat.separator",",");
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(input));
        FileOutputFormat.setOutputPath(conf, new Path(output));
        //Average.column = 2;
        conf.setFloat("mapred.snapshot.frequency", Float.parseFloat("0.50"));
        conf.setBoolean("mapred.map.pipeline", true);
        JobClient.runJob(conf);
 }
}
