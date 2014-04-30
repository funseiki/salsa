
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mapreduce.JobHandler;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path; 

public class QueryProtocol {

    private static final int QUERYOPSUM = 0;
    private static final int QUERYOPAVG = 1;
    private static final int QUERYOPCOUNT = 2;

    private static final int READY = 0; 
    private static final int PROCESSINGQUERY = 1;
    private static final int PROCESSEDQUERY = 2;

    private static final String USAGE = "Usage: <query op> <groupby attribute column> <operation attribute column>; Ex: sum 5 2";
    private static final String SUM = "sum";
    private static final String AVERAGE = "average";
    private static final String INIT_DB = "init_db";
    public int state = READY;

    public PrintWriter clientout;
    
    public JobHandler query_job;

    public QueryProtocol(PrintWriter out)
    {
        this.clientout = out;
    }

    public void cancelQuery()
    {
       try{ 
         query_job.cancelMapReduceJob();
       } catch(Exception e)
       {
          System.err.println("Could not cancel job. " + e);
       }
    }

    public boolean queryStatus()
    {
       return query_job.getStatus();
    }

    public void performMapReduce(String jobtype, String key_i, String col_i)
    {
        try {
             query_job = new JobHandler(clientout);
             //query_job.run("part-m-00000.gz", "/query_out", jobtype, key_i, col_i);
             query_job.run("/yahoo_data/", "/query_out", jobtype, key_i, col_i);
             //System.out.println("FINISHED MAP REDUCE");             
             
        } catch (Exception e) {
             System.err.println("CAUGHT EXCEPTION: " + e.getMessage());
        }
    }
   
    private boolean is_int(String s)
    {

       boolean is_intb = true;
       try{
          int index = Integer.parseInt(s);
       }catch(NumberFormatException e){
          is_intb = false;
      }

      return is_intb;

    }

    /* Send the client the attributes in the db table */
    private void sendClientAttributeList()
    {
        Path path = new Path("/yahoo_data/pig_header");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000/");
        conf.set("fs.default.name", "hdfs://localhost:9000");
        conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-site.xml"));
        conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-default.xml"));
        try{
        FileSystem fs = FileSystem.get(conf);
        if(!fs.exists(path))
        {
            clientout.println("Attribute list file not found");
            return;
        }
        FSDataInputStream in = fs.open(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        clientout.println("START_RESULT");
            line = br.readLine();
            while(line != null)  
            {
                clientout.println(line);
                line = br.readLine();
            }
           br.close();
           in.close();
        clientout.println("END_RESULT");
        }catch(Exception e)
        {
            System.err.println("Exception in SendClientAttrList " + e);
        }
    } // end method sendClientAttribute List

    /* send client the first 10 tuples of the DB */
    private void sendClientDBTuples()
    {
       int i = 0 ;
       int num_tuples = 10;
       Path path = new Path("/yahoo_data/part-r-00000");
       Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000/");
        conf.set("fs.default.name", "hdfs://localhost:9000");
        conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-site.xml"));
        conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-default.xml"));
        try{
        FileSystem fs = FileSystem.get(conf);

        if(!fs.exists(path))
        { 
            clientout.println("The DB file not found");
            return;
        }
        FSDataInputStream in = fs.open(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        //try{
        clientout.println("START_RESULT");
            line = br.readLine();
            while(line != null && i < num_tuples)  
            {
                clientout.println(line);
                line = br.readLine();
                i++;
            }
           br.close();
           in.close();
        clientout.println("END_RESULT");
        }catch(Exception e)
        {
            System.err.println("Exception in SendClienDBTuples " + e);
        }
    } // end method sendClientDBTuples


    public void processInput(String theInput) {
        //List<String> theOutput = new ArrayList();
        if(theInput == null || theInput == "\n")
           {
               //theOutput.add("Ready to process query!");
               clientout.println("Ready to process query!");
               state = READY;
               return;
               //return theOutput;
           }
        if(theInput.equalsIgnoreCase("bye"))
        {
             clientout.println("Bye.");
             state = READY;
        }
        if(theInput.equalsIgnoreCase("attribute_list"))
        {
            sendClientAttributeList();
        }
        if(theInput.equalsIgnoreCase("tuples"))
        {
            sendClientDBTuples();
        }
        
        if (state == READY) {
            if((theInput.toLowerCase().contains(SUM)) || 
               (theInput.toLowerCase().contains(AVERAGE)))
            {
              String[] splits = theInput.split(" ");
              if(splits.length != 3)
              {
                 clientout.println("Error: Invalid number of parameters");
                 clientout.println(USAGE);
              }
              if(is_int(splits[1]) == false || is_int(splits[2]) == false)
              {
                  clientout.println("Error: Attribute's column is not a number");
                  clientout.println(USAGE);
              }
              state = PROCESSINGQUERY;
              
              performMapReduce(splits[0], splits[2], splits[1]);
            }
            else
            {
               clientout.println("Ready to process query!");
            }
        } else if (state == PROCESSINGQUERY) {
            if(theInput.toLowerCase().contains("cancel"))
            {
               cancelQuery();
               clientout.println("Cancelling query");
            }
            System.out.println("Processing query checking if it is done"); 
            if(queryStatus() == true)
            {
               System.out.println("Query is done " + queryStatus() );
               state = READY;
               processInput(theInput);
            }
            else
            {
               clientout.println("PROCESSING_QUERY");
            }
        } 
    }
}
