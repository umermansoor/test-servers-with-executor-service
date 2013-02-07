package com.umansoor;

import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author umermansoor
 */
public class SimpleTcpServer implements Runnable
{
    private final static int MAX_THREADS = 10;
    private final ExecutorService exec;
    private final ServerSocket serverSocket;
    private static final ArrayList<String> randomQuotes = new ArrayList<String>();
    
    static {
        // Initialize some witty qoutes to impress clients with
        randomQuotes.add("It's better to keep your mouth shut and give the impression that you're stupid than to open it and remove all doubt.");
        randomQuotes.add("I was standing in the park wondering why frisbees got bigger as they get closer. Then it hit me.");
        randomQuotes.add("When I die, I want to die like my grandmother, who died peacefully in her sleep. Not screaming like all the passengers in her car.");
        randomQuotes.add("There is no such thing as a stupid question, just stupid people who ask questions.");
        randomQuotes.add("When life gives you lemons, make orange juice and leave the world wondering how the hell you did it.");
        randomQuotes.add("Never take life seriously. Nobody gets out alive anyway.");
        randomQuotes.add("It's just a job. Grass grows, birds fly, waves pound the sand. I beat people up. ----Muhammad Ali.");
        randomQuotes.add("A man's got to believe in something. I believe I'll have another drink.");
        randomQuotes.add("I could tell that my parents hated me. My bath toys were a toaster and a radio.");
        randomQuotes.add("Before I speak, I have something important to say. ");
        randomQuotes.add("I intend to live forever, or die trying.");
    }
    
    public SimpleTcpServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        exec = Executors.newFixedThreadPool(MAX_THREADS);
        
        
    }
    
    
    /**
     * **fucking** ServerSocket.accept() doesn't respond to Thread.interrupt()
     * hence have to manually close the socket.
     * @throws IOException 
     */
    public void closeSocket() throws IOException {
        serverSocket.close();
        
        
    }

    
    
    @Override
    public void run() {
        int numClients = 0;
      
            while(true) {
                System.out.println("waiting for client connection...");
                
                try {
                    exec.execute(new ClientHandler(serverSocket.accept()));
                    
                    System.out.println("client accepted: " + ++numClients);

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                } catch (InterruptedException ie) {
                    break;
                } catch (SocketException e) {
                    break;
                } catch (IOException ioe) {
                    break;
                }
                
            }
            
             exec.shutdownNow();
    }
    
    private class ClientHandler implements Runnable {
        
        private final Socket clientSocket;
  
        public ClientHandler(Socket s) {
            clientSocket = s;
        }
        
        @Override 
        public void run() {
            PrintWriter out = null;
            
            // keep connection open for certain length of time
            final int maxConnectionLength = 1 * 1000 * 60; // 1 minute
            long startTime = System.currentTimeMillis();
            // go into an infinite chitty chat routine with the client
            try {
                out = new PrintWriter(clientSocket.getOutputStream());

                while (true) {
                    // Get a random quote
                    int randIndex = 0 + (int)(Math.random() * ((randomQuotes.size()-1 - 0) + 1));

                    out.write(randomQuotes.get(randIndex) + "\r\n");
                    out.flush();
                    Thread.sleep(3000);

                    if (System.currentTimeMillis() - startTime > maxConnectionLength) {
                        // Maximum session length reached. Timeout
                        out.write("bye\r\n");
                        out.flush();
                        clientSocket.shutdownOutput();
                        clientSocket.close();
                    }
                }
            
            } catch (IOException ioe) {
                return;
                
            } catch (InterruptedException ie) {
                return;
            }
            
        }
        
    }
   
}
