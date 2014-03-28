
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
    

    public List<String> performMapReduce()
    {
        List<String> qresults = new ArrayList();
        try {
             QuerySumMapReduce qs = new QuerySumMapReduce();
             qs.run("yahoo_data", "query_out", "5", "3");
             System.out.println("FINSIHED MAP REDUCE");             
             ReadSnapshot rs = new ReadSnapshot("query_out");
             if(rs.validateFile())
                qresults = rs.readFile();             
             for(String temp : qresults) 
             {
                 System.out.println(temp);
             }
             
        } catch (Exception e) {
             System.err.println("CAUGHT EXCEPTION: " + e.getMessage());
        }
        try {
             ReadSnapshot rs = new ReadSnapshot("query_out");
             if(rs.validateFile())
                qresults = rs.readFile();             
             for(String temp : qresults) 
             {
                 System.out.println(temp);
             }
        } catch(Exception e){
             System.err.println("CAUGHT Exception in reading snapshot: " + e.getMessage());
        }
        return qresults;
    }
 
    public List<String> processInput(String theInput) {
        List<String> theOutput = new ArrayList();
        if(theInput == null)
           {
               theOutput.add("Ready to process query!");
               return theOutput;
           }
        if(theInput.equalsIgnoreCase("bye"))
        {
             theOutput.add("Bye");
             state = READY;
        }
        if (state == READY) {
            if(theInput.toLowerCase().contains(SUM))
            {
              return performMapReduce();
            }
            theOutput.add("Ready to process query!");
            state = PROCESSINGQUERY;
        } else if (state == PROCESSINGQUERY) {
            theOutput.add("Processing query!");
            state = READY;
        } 
        return theOutput;
    }
}
