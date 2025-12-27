import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A Chat Client.
 * This class can be compiled and run independently of the server project.
 */
public class ClientApp {
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        // Configuration: Localhost for same machine, or use Server IP address
        String host = "localhost";
        int port = 8080; 

        try (Socket socket = new Socket(host, port)) {
            System.out.println("--- Connected to Chat Server on " + host + ":" + port + " ---");

            // Independent I/O setup using standard Java classes
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner console = new Scanner(System.in);

            // THREAD 1: RECEIVER (Handles incoming stream)
            Thread receiver = new Thread(() -> {
                try {
                    String msg;
                    while (isRunning && (msg = in.readLine()) != null) {
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                        System.out.println("\n[" + time + "] Server: " + msg);
                        System.out.print("> "); 
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        System.out.println("\n[System] Connection to server closed.");
                    }
                }
            });

            // THREAD 2: SENDER (Handles user keyboard input)
            Thread sender = new Thread(() -> {
                try {
                    while (isRunning) {
                        if (console.hasNextLine()) {
                            String input = console.nextLine();
                            if ("exit".equalsIgnoreCase(input) || "8".equals(input)) {
                                isRunning = false;
                                out.println("8"); // Signal server for disconnect
                                break;
                            }
                            out.println(input);
                        }
                    }
                } catch (Exception e) {
                    isRunning = false;
                }
            });

            receiver.start();
            sender.start();

            // Wait for threads to finish
            receiver.join();
            sender.join();
            
            System.out.println("Client closed. Goodbye!");

        } catch (IOException e) {
            System.err.println("Error: Could not connect to server. Ensure it is running on port " + port);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Client was interrupted.");
        }
    }
}