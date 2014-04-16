import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
 
public class QueryDaemon {
    public static void main(String[] args) throws IOException {
         
        if (args.length != 1) {
            System.err.println("Usage: java QueryDaemon <port number>");
            System.exit(1);
        }
 
        int portNumber = Integer.parseInt(args[0]);
        boolean close_client = false;
 
        try{ 
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
         
            String inputLine;
            List<String> outputLine;
             
            // Initiate conversation with client
            QueryProtocol qp = new QueryProtocol(out);
            outputLine = qp.processInput(null);
            for(String outL : outputLine)
               out.println(outL);
            
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client: " + inputLine);
                outputLine = qp.processInput(inputLine);
           /* System.out.println(outputLine);
                for(String outL : outputLine)
                {
                    out.println(outL);
                    if(out.equals("Bye"))
                      close_client = true;
                }
                //out.println(outputLine);
                if (close_client == true)
                    break; */
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}

