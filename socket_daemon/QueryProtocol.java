
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
 
public class QueryProtocol {

    private static final int QUERYOPSUM = 0;
    private static final int QUERYOPAVG = 1;
    private static final int QUERYOPCOUNT = 2;

    private static final int READY = 0; 
    private static final int PROCESSINGQUERY = 1;
    private static final int PROCESSEDQUERY = 2;

    private static final String SUM = "sum";
    private static final String AVG = "avg";
    private int state = READY;
    

    public List<String> performMapReduce(String key_i, String col_i)
    {
        List<String> qresults = new ArrayList();
        try {
             QuerySumMapReduce qs = new QuerySumMapReduce();
             //qs.run("yahoo_data", "query_out", "5", "3");
             qs.run("yahoo_data", "query_out", key_i, col_i);
             System.out.println("FINSIHED MAP REDUCE");             
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
 
    public List<String> processInput(String theInput) {
        List<String> theOutput = new ArrayList();
        if(theInput == null || theInput == "")
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
            if(theInput.toLowerCase().contains(SUM))
            {
              String[] splits = theInput.split(" ");
              state = PROCESSINGQUERY;
              return performMapReduce(splits[1], splits[2]);
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
