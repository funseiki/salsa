import java.io.File;
import java.io.IOException;
 
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.hadoop.io.IOUtils;


public class ReadSnapshot{

   private Path path;
   private FileSystem fs;
   
   public ReadSnapshot(String readpath) throws Exception
   {
       path = new Path(readpath);
       fs = FileSystem.get(new Configuration());
   }

   public boolean validateFile() throws Exception
   {
      if(!fs.exists(path))
         return false;
      if(!fs.isFile(path))
         return false;
      return true;
   } 

   public List<String> readFile() throws Exception
   {
       FileStatus[] items = fs.listStatus(path);
       if (items == null) return new ArrayList<String>();
       List<String> results = new ArrayList<String>();
       for(FileStatus item: items)
       {
          if(item.getPath().getName().startsWith("_"))
          {
             continue;
          }
          FSDataInputStream in = fs.open(item.getPath());
          
          byte buffer[] = new byte[1024];
          int bytesRead = 0;
          while((bytesRead = in.read(buffer)) != -1  )
          {
         //   System.out.println(new String(buffer, 0, bytesRead, "UTF-8"));
            String raw = new String(buffer, 0, bytesRead, "UTF-8");
            String [] result = raw.split("\n");
            for(String str: raw.split("\n"))
            {
                results.add(str);
            }  
           // results.add(raw);
          }
       }
       
       fs.delete(path, true);
       return results;
   }

   public static void main(String args[]) throws Exception
   {
       ReadSnapshot rs = new ReadSnapshot(args[0]);
//       if(rs.validateFile())
          rs.readFile();

   }
}
