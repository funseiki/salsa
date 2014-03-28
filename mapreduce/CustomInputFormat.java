import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobConfigurable;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.*;


/* Set custom input format so key in map can be set to specific columns
*/
public class CustomInputFormat extends FileInputFormat<Text, Text> 
{
        public RecordReader<Text, Text> getRecordReader(
           InputSplit input, JobConf job, Reporter reporter)
           throws IOException {

           reporter.setStatus(input.toString());
           return new CustomRecordReader(job, (FileSplit)input);
        }
     }

class CustomRecordReader implements RecordReader<Text, Text> {

      private LineRecordReader lineReader;
      private LongWritable lineKey;
      private Text lineValue;
      private String op_on_col;

      public CustomRecordReader(JobConf job, FileSplit split) throws IOException 
      {
          lineReader = new LineRecordReader(job, split);

          lineKey = lineReader.createKey();
          lineValue = lineReader.createValue();
          op_on_col = job.getJobName();
      }

      public boolean next(Text key, Text value) throws IOException {
         // get the next line
         if (!lineReader.next(lineKey, lineValue)) {
             return false;
         }
         value.set(lineValue);
        // now that we know we'll succeed, overwrite the output objects

        key.set(op_on_col.trim()); // The column index to perform sum on is the output key.

        return true;
     }

     public Text createKey() {
       return new Text("");
     }

     public Text createValue() {
       return new Text("");
     }

     public long getPos() throws IOException {
       return lineReader.getPos();
     }

     public void close() throws IOException {
        lineReader.close();
     }

     public float getProgress() throws IOException {
       return lineReader.getProgress();
     }

}

