
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mapreduce.JobHandler;
 
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
    private int state = READY;

    public PrintWriter clientout;
    
    public JobHandler query_job;

    public QueryProtocol(PrintWriter out)
    {
        this.clientout = out;
    }

    public List<String> performMapReduce(String jobtype, String key_i, String col_i)
    {
        List<String> qresults = new ArrayList();
        try {
             query_job = new JobHandler(clientout);
             query_job.run("yahoo_data", "query_out", jobtype, key_i, col_i);

             //System.out.println("FINISHED MAP REDUCE");             
             ReadSnapshot rs = new ReadSnapshot("query_out");
                qresults = rs.readFile();
             //System.out.println(qresults);             
            
             for(String temp : qresults) 
             {
                 System.out.println(temp);
             }
             
        } catch (Exception e) {
             System.err.println("CAUGHT EXCEPTION: " + e.getMessage());
        }
        return qresults;
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

    //public List<String> createJob
    public List<String> processInput(String theInput) {
        List<String> theOutput = new ArrayList();
        if(theInput == null || theInput == "\n")
           {
               theOutput.add("Ready to process query!");
               state = READY;
               return theOutput;
           }
        if(theInput.equalsIgnoreCase("bye"))
        {
             theOutput.add("Bye");
             state = READY;
             return theOutput;
        }
        if (state == READY) {
            if((theInput.toLowerCase().contains(SUM)) || 
               (theInput.toLowerCase().contains(AVERAGE)))
            {
              String[] splits = theInput.split(" ");
              if(splits.length != 3)
              {
                 theOutput.add("Error: Invalid number of parameters");
                 theOutput.add(USAGE);
                 return theOutput;
              }
              if(is_int(splits[1]) == false || is_int(splits[2]) == false)
              {
                  theOutput.add("Error: Attribute's column is not a number");
                  theOutput.add(USAGE);
                  return theOutput;
              }
              state = PROCESSINGQUERY;
              
              return performMapReduce(splits[0], splits[1], splits[2]);
            }
            else
            {
               //theOutput.add("Invalid client operation "+ theInput);
               theOutput.add("Ready to process query!"); 
               return theOutput;
            }
        } else if (state == PROCESSINGQUERY) {
            if(theInput.toLowerCase().contains("cancel"))
               theOutput.add("Cancelling query");
            state = READY;
        } 
        return theOutput;
    }
}
