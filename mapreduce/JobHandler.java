package mapreduce;

import java.io.*;
import java.util.*;
import java.net.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

interface ThreadCompleteListener {
    void notifyOfThreadComplete(Runnable runner);
}


abstract class NotificationThread implements Runnable{

    /**
     * An abstract function that children must implement. This function is where 
     * all work - typically placed in the run of runnable - should be placed. 
     */

    public abstract void startJob();
    /**
     * Our list of listeners to be notified upon thread completion.
     */

    private java.util.List<ThreadCompleteListener> listeners = Collections.synchronizedList( new ArrayList<ThreadCompleteListener>() );
    /**
     * Adds a listener to this object. 
     * @param listener Adds a new listener to this object. 
     */

    public void addListener( ThreadCompleteListener listener ){
        listeners.add(listener);
    }

    /**
     * Removes a particular listener from this object, or does nothing if the listener
    * is not registered. 
     * @param listener The listener to remove. 
     */

    public void removeListener( ThreadCompleteListener listener ){
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners that the thread has completed.
     */

    private final void notifyListeners() {
        synchronized ( listeners ){
            for (ThreadCompleteListener listener : listeners) {
              listener.notifyOfThreadComplete(this);
            }
        }
    }

    /**
     * Implementation of the Runnable interface. This function first calls doRun(), then
     * notifies all listeners of completion.
     */

    public void run(){
        try{
            startJob();
        }
        finally
        {
            notifyListeners();
        }
    }

}

class MapReduceJob extends NotificationThread implements Runnable
{
    Thread t;
    String input, output;
    String jobtype;
    String jobGroupby;
    String jobColumn;
    PrintWriter clientout;

    public MapReduceJob(String input, String output, String jobtype, String jobGroupby, String jobColumn, PrintWriter clientout)
    {
        t = new Thread(this);
        this.input = input;
        this.output = output;
        this.jobtype = jobtype;
        this.jobGroupby = jobGroupby;
        this.jobColumn = jobColumn;
        this.clientout = clientout;
        t.start();
    }

    public void startJob()
    {
        int i = 0;
        System.out.println("Starting MapReduce Job...");
        try
        {
            if(jobtype.toLowerCase().equals("sum"))
            {
                Sum sum_job = new Sum();
                sum_job.run(input, output, jobGroupby, jobColumn);        
            }
            if(jobtype.toLowerCase().equals("average"))
            {
                Average avg_job = new Average();
                avg_job.run(input, output, jobGroupby, jobColumn);
            }
            /*switch(jobtype.toLowerCase())
            {
               case "sum":
                     Sum sum_job = new Sum();
                     sum_job.run(input, output, jobGroupby, jobColumn);        
                     break;
               case "average":
                     Average avg_job = new Average();
                     avg_job.run(input, output, jobGroupby, jobColumn);
                     break;
               default:
                 System.out.println("Invalid job type " + jobtype);
            } */
            //Average job = new Average();
            //Make this generic
            //job.run(input,output,"2","2");
            //job.run(input,output,jobGroupby, jobColumn);
        }
        catch(Exception e)
        {
            System.out.println(e);
            return;
        }
        System.out.println("Finshed MapReduce Job");
        
    }

    public void join() throws InterruptedException
    {
        t.join();
    }
}


class Poll implements Runnable, ThreadCompleteListener
{
    Thread t;
    String dirPath;
    NotificationThread mapReduceThread;
    Boolean mapReduceComplete;
    String mostRecentSnapshot;
    Path snapshotPath;
    FileSystem fs;
    PrintWriter clientout;

	public Poll(PrintWriter clientout, String dirPath, NotificationThread mapReduceThread)
	{
        t = new Thread(this);
        this.clientout = clientout;
        this.mapReduceThread = mapReduceThread;
        this.mapReduceThread.addListener(this);
        mapReduceComplete = false;
    	this.dirPath = dirPath;
        t.start();
	}

	public void performPoll()
	{
        try{
            Configuration conf = new Configuration();
            //conf.set("fs.defaultFS", "hdfs://localhost:54310/tmp");
            conf.set("fs.defaultFS", "hdfs://localhost:9000/");
            //conf.set("fs.default.name", "hdfs://localhost:54310");
            conf.set("fs.default.name", "hdfs://localhost:9000");
      //      conf.addResource(new Path("/usr/local/hadoop/conf/hadoop-site.xml"));
        //    conf.addResource(new Path("/usr/local/hadoop/conf/hadoop-default.xml"));
            conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-site.xml"));
            conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-default.xml"));
            fs = FileSystem.get(conf);
            mostRecentSnapshot = null;
            long snapshotTime = -1;
            while(!mapReduceComplete)
            {
                Thread.sleep(3000);
                FileStatus[] status = fs.listStatus(new Path(dirPath));;
                for (int i=0;i<status.length;i++){
                Path path = status[i].getPath();
                String fileName = path.getName();
                if(fileName.contains("snapshot"))
                {
                    if(mostRecentSnapshot == null || status[i].getModificationTime() > snapshotTime)
                    {
                        mostRecentSnapshot = fileName;
                        snapshotTime = status[i].getModificationTime();
                        snapshotPath = path;
                    }
                }
                System.out.println("------------------------------------");
                System.out.println(mostRecentSnapshot+" "+snapshotTime);
                System.out.println("------------------------------------");
                
               // } // end for
               if(mostRecentSnapshot != null && mostRecentSnapshot.contains("snapshot"))
                {
                    FSDataInputStream in = fs.open(snapshotPath);

                    byte buffer[] = new byte[2048];
                    int bytesRead = 0;
                    while((bytesRead = in.read(buffer)) != -1  )
                    {
                       System.out.println(new String(buffer, 0, bytesRead, "UTF-8"));
                       String raw = new String(buffer, 0, bytesRead, "UTF-8");
                       String [] result = raw.split("\n");
                       for(String str: raw.split("\n"))
                       {
                         clientout.println(str);
                       }
                    }
                 } 
                } // end for 
              } // map reduce complete
            }catch(Exception e){
		System.out.println(e);
                System.out.println("File not found");
            }
            clientout.println("Ready to process query!");
    }
    public void notifyOfThreadComplete(Runnable runner)
    {
        mapReduceComplete = true;
    }

    public void run()
    {
        performPoll();
    }

    /*String may not be the right data structure to return here*/
    public Path getSnapshotPath() //throws IOException
    {
        return snapshotPath;
    }

    public void join() throws InterruptedException
    {
        t.join();
    }
}




public class JobHandler{
    public Poll p1;

    public MapReduceJob t1;

    public PrintWriter clientout;
 
    public JobHandler(PrintWriter out)
    {
       this.clientout = out;
    }

    public void cancelMapReduceJob()
    {

    }

    public boolean getStatus()
    {
       return p1.mapReduceComplete;  
    }
 
    public void run(String input, String output, String jobtype, String jobGroupby, String jobColumn) throws InterruptedException
    {
        t1 = new MapReduceJob(input, output, jobtype, jobGroupby, jobColumn, clientout);
        p1 = new Poll(clientout, output, t1);   
        System.out.println("Created polling and mapred job");
    }


    public Path getSnapshotPath()
    {
        return p1.getSnapshotPath();
    }

    public static void main (String [] args) throws Exception{
	//   String input = "/tmp/input/test.csv";
	   String input = "yahoo_data";
       String output = "average_output";
       PrintWriter stdout = new PrintWriter(System.out);
       JobHandler job = new JobHandler(stdout);
       job.run(input, output,args[0],args[1], args[2] );
    }

}
