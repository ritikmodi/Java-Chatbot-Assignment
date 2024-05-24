
package org.example;


import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private static final String host = "localhost";
    private static final int port = 1234;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static Socket socket;
    private static boolean running = true;
//    private static Logger logger = new Logger();

    public static void main(String[] args) throws IOException {
        setupLogger();
        addShutdownHookClient();

        try {
            socket = new Socket(host, port);
//            System.out.println("client socket="+socket.getChannel());
            System.out.println("Connected to the chat server!");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            PrintWriter finalOut = out;
            BufferedReader finalIn = in;

            threadPool.execute(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = finalIn.readLine()) != null) {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    if(!socket.isClosed()) {
                        e.printStackTrace();
                        logEvent("Error reading server response : " + e.getMessage());
                    }
                    running = false;
                } finally {
                    closeResources(finalOut, finalIn, socket);
                }
            });


            Scanner scanner = new Scanner(System.in);
            String userInput;

            while (running) {
                if(scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                out.println(userInput);
                if(userInput.equalsIgnoreCase("exit")) {
                    running = false;
                    closeResources(socket);
                    addShutdownHookClient();
                    //break;
                }
            }
                else {
                    running = false;
                }
        }
        } catch (IOException e) {
            e.printStackTrace();
            logEvent("Client error : " + e.getMessage());
        } finally {
            closeResources(socket);
            addShutdownHookClient();
            // closeResources(null, null, socket);
            threadPool.shutdown();
        }
    }

    public static void logEvent(String message) {
        try(FileWriter fw = new FileWriter("server.log", true)) {
            fw.write(message + "\n");
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public static void setupLogger() {
        try {
            File logFile = new File("server.log");
            if(!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public static void addShutdownHookClient() {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            System.out.println("Client is shutting down");
            logEvent("Client is shutting down");
            running = false;
            closeResources(socket);
            threadPool.shutdownNow();
        }));
    }

    public static void closeResources(Socket socket) {
        try {
            running = false;

            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ie) {
            logEvent("Error closing resources " + ie.getMessage());
        }
    }

    public static void closeResources(PrintWriter out, BufferedReader in, Socket socket) {
        try {
            running = false;
            if(out != null)
                out.close();

            if(in != null)
                in.close();

            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ie) {
            logEvent("Error closing resources " + ie.getMessage());
        }
    }
}
