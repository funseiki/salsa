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

    public MapReduceJob()
    {
        t = new Thread(this);
        t.start();
    }

    public void setJobArgs()
    {

    }

    public void startJob()
    {
        int i = 0;
        System.out.println("Starting MapReduce Job...");
        try
        {
            Average job = new Average();
            job.run("/tmp/input/JobHandler.csv","/tmp/output/average_output","2","2");
        }
        catch(Exception e)
        {
            System.out.println(e);
            return;
        }
        System.out.println("Finshed MapReduce Job");
        
    }
}



class Poll implements Runnable, ThreadCompleteListener
{
    Thread t;
    String dirPath;
    NotificationThread mapReduceThread;
    Boolean mapReduceComplete;

	public Poll(String dirPath, NotificationThread mapReduceThread)
	{
        t = new Thread(this);
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
            conf.set("fs.defaultFS", "hdfs://localhost:54310/tmp");
            conf.set("fs.default.name", "hdfs://localhost:54310");
            conf.addResource(new Path("/usr/local/hadoop/conf/hadoop-site.xml"));
            conf.addResource(new Path("/usr/local/hadoop/conf/hadoop-default.xml"));
            FileSystem fs = FileSystem.get(conf);
            String mostRecentSnapshot = null;
            long snapshotTime = -1;
            while(!mapReduceComplete)
            {
                FileStatus[] status = fs.listStatus(new Path(dirPath));;
                for (int i=0;i<status.length;i++){
                String path = status[i].getPath().getName();
                if(path.contains("snapshot"))
                {
                    if(mostRecentSnapshot == null || status[i].getModificationTime() > snapshotTime)
                    {
                        mostRecentSnapshot = path;
                        snapshotTime = status[i].getModificationTime();
                    }
                }
                }
                Thread.sleep(2000);
            }
            
            System.out.println(mostRecentSnapshot+" "+snapshotTime);
            }catch(Exception e){
		System.out.println(e);
                System.out.println("File not found");
            }
    }
    public void notifyOfThreadComplete(Runnable runner)
    {
        mapReduceComplete = true;
    }

    public void run()
    {
        performPoll();
    }

}




public class JobHandler{
    public static void main (String [] args) throws Exception{
	NotificationThread t1 = new MapReduceJob();
    Poll p1 = new Poll("/tmp/output/average_output", t1);
    }
}
