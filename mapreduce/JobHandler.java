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
    Sum sum_job;
    Average avg_job;

    public MapReduceJob(String input, String output, String jobtype, String jobGroupby, String jobColumn)
    {
        t = new Thread(this);
        this.input = input;
        this.output = output;
        this.jobtype = jobtype;
        this.jobGroupby = jobGroupby;
        this.jobColumn = jobColumn;
        t.start();
    }
 
    public String getJobId()
    {
        if(jobtype.toLowerCase().equals("average"))
        {
            return avg_job.getJobId();
        }
        return "";
    }

    public void stopJob() throws Exception
    {
       if(jobtype.toLowerCase().equals("average"))
       {

           avg_job.stopJob();

       }
       else
       {

          sum_job.stopJob();

       }  
     //  t.interrupt();
    }

    public void startJob()
    {
        int i = 0;
        System.out.println("Starting MapReduce Job...");
        try
        {
            if(jobtype.toLowerCase().equals("sum"))
            {
                sum_job = new Sum();
                sum_job.run(input, output, jobGroupby, jobColumn);        
            }
            if(jobtype.toLowerCase().equals("average"))
            {
                avg_job = new Average();
                avg_job.run(input, output, jobGroupby, jobColumn);
            }
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
    //MapReduceJob mapReduceThread;
    Boolean mapReduceComplete;
    String mostRecentSnapshot;
    Path snapshotPath;
    FileSystem fs;
    PrintWriter clientout;
    String jobType;

	public Poll(PrintWriter clientout, String dirPath, NotificationThread mapReduceThread)
//	public Poll(PrintWriter clientout, String dirPath, MapReduceJob  mapReduceThread)
	{
        t = new Thread(this);
        this.clientout = clientout;
       
        this.mapReduceThread = mapReduceThread;
        this.mapReduceThread.addListener(this);
        mapReduceComplete = false;
        System.out.println("path of snapshot file: " + dirPath);
    	//this.dirPath = dirPath;
    	this.dirPath = "/query_out/";
        System.out.println("path of snapshot file: " + this.dirPath);
       // this.dirPath = "/tmp";
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
            conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-site.xml"));
            conf.addResource(new Path("/hadoop/hadoop-hop-0.2/conf/hadoop-default.xml"));
            fs = FileSystem.get(conf);

            clientout.println("START_RESULT");
            mostRecentSnapshot = null;
            long snapshotTime = -1;
            String orig_dirPath = new String(dirPath);
            boolean dir_visited = false;
            while(!mapReduceComplete)
            {
                Thread.sleep(500);
      
                Path out_dir_path = new Path(dirPath);
                if(fs.exists(out_dir_path))
                {
                //System.out.println("Path of output " + out_dir_path.toString());
                FileStatus[] status = fs.listStatus(out_dir_path);;
                for (int i=0;i<status.length;i++){
                     Path path = status[i].getPath();
                     String fileName = path.getName();
                     if(fileName.contains("temporary") && dir_visited == false)
                     {
                         dirPath = path.toString();
                         dirPath.concat("/");
                         System.out.println("NEW DIR PATH " + dirPath);
                         break;
                     }
                     if(fileName.contains("attempt") && dir_visited == false)
                     {
                         dirPath = path.toString();
                         dirPath.concat("/");
                         System.out.println("NEW DIR PATH " + dirPath);
                         break;
                     }
                     if(fileName.contains("snapshot-00001") && dir_visited == false)
                     {
                         dirPath = orig_dirPath;
                         System.out.println("NEW DIR PATH " + dirPath);
                         snapshotTime = snapshotTime - 5; 
                         dir_visited = true;
                         break;
                     } 
                 //    System.out.println("Files in dir: " + fileName + " Modified on " + status[i].getModificationTime());
                     if(fileName.contains("snapshot")  && snapshotTime <= status[i].getModificationTime())
                    // if(fileName.contains("snapshot")  && status[i].getLen() > 0 && status[i].getModificationTime() > snapshotTime)

                    {
                        try{
                        FSDataInputStream in = fs.open(path);
                        System.out.println(status[i].getModificationTime()+" "+ fileName);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String line;
                        line = br.readLine();
                        if(line == null)
                          break;
                        String[] tokens = fileName.split("-");
                        if(tokens.length != 3)
                           break;
                        for(int j =0;j<tokens.length;j++)
                           System.out.println(tokens[j]);
                        clientout.println("START_SNAPSHOT");
                        while(line != null)
                        {
                            String progressLine = line + "," + tokens[1];
                            System.out.println(progressLine);
                            clientout.println(progressLine);
                            line = br.readLine();
                        }
                        br.close();
                        in.close();
                        /*
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
                        } */
                        clientout.println("END_SNAPSHOT");
                        } catch (Exception e)
                        {
                            System.out.println("While reading file exception happended. probably the temp file is moved.");
                            dirPath = orig_dirPath;
                            System.out.println("NEW DIR PATH " + dirPath);
                            dir_visited = true;
                        }// end of try reading the file
                        
                    }
                    
                     if(fileName.contains("snapshot"))
                     //if(fileName.contains("tmp"))
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
                
                 } // end for 
               }// if file exists
              } // map reduce complete
              
            }catch(Exception e){
		System.out.println(e);
                System.out.println("File not found");
            }
            clientout.println("END_RESULT");
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

    public void cancelMapReduceJob() throws Exception
    {
        if(p1.mapReduceComplete == false)
         t1.stopJob(); 
        p1.mapReduceComplete = true;
    }

    public boolean getStatus()
    {
       return p1.mapReduceComplete;  
    }
 
    public void run(String input, String output, String jobtype, String jobGroupby, String jobColumn) throws InterruptedException
    {
        t1 = new MapReduceJob(input, output, jobtype, jobGroupby, jobColumn);
        p1 = new Poll(clientout, output, t1);   
        System.out.println("Created polling and mapred job");
        System.out.println("MAP RED JOB ID " + t1.getJobId());
    }


    public Path getSnapshotPath()
    {
        return p1.getSnapshotPath();
    }

    public static void main (String [] args) throws Exception{
	//   String input = "/tmp/input/test.csv";
	   String input = "/yahoo_data/part-r-00000";
       String output = "average_output";
       PrintWriter stdout = new PrintWriter(System.out);
       JobHandler job = new JobHandler(stdout);
       job.run(input, output,args[0],args[1], args[2] );
    }

}
