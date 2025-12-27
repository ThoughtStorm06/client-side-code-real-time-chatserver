import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * A simplified, robust Chat Client for the thoughtstorm06 server.
 */
public class ClientApp {
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner console = new Scanner(System.in)) {

            System.out.println("=== Connected to Chat Server ===");

            // THREAD 1: Listen for messages from the server
            Thread receiver = new Thread(() -> {
                try {
                    String serverMessage;
                    while (isRunning && (serverMessage = in.readLine()) != null) {
                        // Print server messages directly without the ">" prefix
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    if (isRunning) System.out.println("[System] Lost connection to server.");
                }
            });
            receiver.start();

            // THREAD 2: Main thread handles keyboard input
            while (isRunning) {
                if (console.hasNextLine()) {
                    String input = console.nextLine();
                    out.println(input);

                    // If user chooses exit (choice 8 or typing 'exit')
                    if ("8".equals(input) || "exit".equalsIgnoreCase(input)) {
                        isRunning = false;
                    }
                }
            }

            System.out.println("Disconnecting... Goodbye!");
            socket.close(); // Ensure socket is closed

        } catch (IOException e) {
            System.err.println("Could not connect to server. Is it running?");
        }
    }
}