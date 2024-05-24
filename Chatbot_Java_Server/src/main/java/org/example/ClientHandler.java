package org.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private final ConcurrentHashMap<String, ClientHandler> clients;
    private static final Logger logger = new Logger();
    private Server server;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, ClientHandler> clients, Server server) {
        this.clientSocket = socket;
        this.clients = clients;
        this.server = server;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ie) {
            ie.printStackTrace();
            logger.logEvent("Error creating streams : " + ie.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            username = getUsername();
            System.out.println("User " + username + " connected");
            logger.logEvent("User " + username + " connected");

            clients.put(username, this);
            server.notifyAllClients("User " + username + " joined...");
            sendActiveClientsList();

//              out.println("Welcome to the chat, " + username + "!");

            String inputLine;
            out.println("Enter the Type of Message, 'broadcast' OR 'private' or 'exit' to exit :");

            while (((inputLine = in.readLine()) != null) && (!inputLine.equalsIgnoreCase("exit"))) {

                if (inputLine.equalsIgnoreCase("broadcast")) {
                    out.println("Enter your broadcast message");
                    String message = in.readLine();
                    System.out.println("[" + username + "]: " + message);
                    logger.logEvent("[" + username + "]: " + message);
                    server.broadcast("[" + username + "]: " + message, this);
                } else if (inputLine.equalsIgnoreCase("private")) {
                    out.println("Enter receiver name : ");
                    String receiver = in.readLine();
                    out.println("Type Your Private Message");
                    String message = in.readLine();
                    boolean result = server.privateMessage(message, receiver, this);
                    if(result) {
                        System.out.println("[" + this.username + "] " + "to [" + receiver + "] : " + message);
                        logger.logEvent("[" + this.username + "] " + "to [" + receiver + "] : " + message);
                    }
                    else {
                        System.out.println("Receiver [" + receiver + "] Not Found");
                        logger.logEvent("Receiver [" + receiver + "] Not Found");
                    }
                }
                out.println("Enter the Type of Message, Broadcast OR PrivateMessage or 'exit' to exit :");
            }

            clients.remove(username);
            System.out.println("User " + username + " disconnected...");
            server.notifyAllClients("User " + username + " left...");
            System.out.println("Calling closeResources====");
            closeResources();

        } catch (IOException ie) {
            logger.logEvent("Client Handler error : " + ie.getMessage());
        } finally {
            closeResources();
        }
    }


    private String getUsername() throws IOException {
        out.println("Enter your username:");
        return in.readLine();
    }


    public void sendMessage(String message) {
        out.println(message);
        //out.println("Type Your Message");
    }

    public void sendActiveClientsList() {
        StringBuilder activeClients = new StringBuilder("Active Clients : ");
        for(String clientName : clients.keySet()) {
            activeClients.append(clientName).append(", ");
        }
        sendMessage(activeClients.toString());
    }

    public String getUsername1() {
        return username;
    }

    public void closeResources() {
        try {
            out.println("closing resources for " + getUsername1());
            if(clientSocket != null && !clientSocket.isClosed()) {
                out.println("line 227=================="+ clientSocket.isClosed());
                clientSocket.close();
            }

            if(out != null)
                out.close();

            if(in != null)
                in.close();

        } catch (IOException ie) {
            logger.logEvent("Error closing resources : " + ie.getMessage());
        }
    }
}