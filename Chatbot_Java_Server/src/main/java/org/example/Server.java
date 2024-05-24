package org.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import org.example.Client.*;

public class Server {
    private static final int port = 1234;
    private static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static ServerSocket serverSocket;
    private static Logger logger = new Logger();
    private static Server server = new Server();
//    private static Socket clientSocket;

    public static void main(String[] args) throws InterruptedException {
        logger.setupLogger();
        addShutdownHookServer();

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running and waiting for connections..");
            logger.logEvent("Server is running and waiting for connections..");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                logger.logEvent("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients, server);
                threadPool.execute(clientHandler);
//                clients.put(clientHandler);
//                new Thread(clientHandler).start();
            }
        } catch (IOException ie) {
            if(serverSocket != null && !serverSocket.isClosed()) {
                ie.printStackTrace();
                logger.logEvent("Server error : " + ie.getMessage());
            }
        } finally {
            shutdownServer();
//            threadPool.shutdown();
//            closeServerSocket();
        }
    }

    public static void broadcast(String message, ClientHandler sender)
    {
        for (ClientHandler client : clients.values()) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static boolean privateMessage(String message, String receiver, ClientHandler sender)
    {
        ClientHandler receiverHandler = clients.get(receiver);
        if(receiverHandler != null && receiverHandler != sender) {
            receiverHandler.sendMessage("[Private from " + sender.getUsername1() + "] : " + message);
            return true;
        }
        else {
            sender.sendMessage("Receiver " + receiver + " not found");
            return false;
        }
    }

    public static void notifyAllClients(String message) {
        for(ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public static void addShutdownHookServer() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownServer()));
    }

    private static void shutdownServer() {
        System.out.println("Server is shutting down addShutdownHook server");
        logger.logEvent("Server is shutting down LogEvent");
        notifyAllClients("Server is shutting down");

        for (ClientHandler client : clients.values()) {
            System.out.println("client values ===" + clients.values());
            client.closeResources();
        }

        threadPool.shutdownNow();
        closeServerSocket();
    }

    public static void closeServerSocket() {
        try {
            if(serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ie) {
            logger.logEvent("Error closing Server Socket " + ie.getMessage());
        }
    }
}
