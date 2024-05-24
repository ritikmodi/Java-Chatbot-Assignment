package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

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
}
