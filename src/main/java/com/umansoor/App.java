package com.umansoor;

import java.io.*;
import java.util.concurrent.*;
import java.net.*;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException, ExecutionException
    {
        final int serverPort = 3133;
        final int MAX_THREADS = 2;
        final LinkedList<Future<Boolean>> clientsList = 
                new LinkedList<Future<Boolean>>();
        
        System.out.println( "Hello World!" );
        SimpleTcpServer tcp = new SimpleTcpServer(serverPort);
        Thread serverThread = new Thread(tcp);
        serverThread.start();
        
        ExecutorService exec = Executors.newFixedThreadPool(MAX_THREADS);
        for (int i=0; i < MAX_THREADS; i++) {
            clientsList.add(exec.submit(new SimpleTcpClient(serverPort, i)));
        }
        
        // Keep checking clients continously
        for (;;) { // sick of while(true)
            ListIterator<Future<Boolean>> itb = clientsList.listIterator();
            if (itb.hasNext() == false)
                break;
            
            while (itb.hasNext()) {
                Future<Boolean> client = itb.next();
                
                try {
                    Boolean b = client.get(1, TimeUnit.SECONDS);
                     if (!b) {
                        System.out.println("done");
                    
                    }
                     else {
                         System.out.println("clean");
                         itb.remove();
                         
                     }
                    
                } catch (TimeoutException te) {
                    
                }
               
            }
        }
                
        System.exit(0);

    }
}


class SimpleTcpClient implements Callable<Boolean>
{
    private final int port;
    private final int clientId;
    
    public SimpleTcpClient(int port, int clientId) {
        this.port = port;
        this.clientId = clientId;
    }
    
    @Override
    public Boolean call() throws UnknownHostException {
        try {
            Socket clientSocket = new Socket("localhost", port);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            String data;
            
            while ( ( data = in.readLine()) != null) {
                
                System.out.println("+client " + clientId + " (from server): " + data);
            }
            
            // server closed socket, signal clean termination
            return Boolean.TRUE;
        } catch (IOException ioe) {
            return Boolean.FALSE;
        }

        
    }
    
}