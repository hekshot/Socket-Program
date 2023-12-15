import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SimpleServer <port>");
            return;
        }

        int portNumber = Integer.parseInt(args[0]);

        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Server is listening on port " + portNumber);

            // Wait for a client to connect
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // Create input and output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Start a separate thread to handle messages from the client
            new Thread(() -> handleClientMessages(in, out)).start();

            // Start a separate thread to handle messages from the server console
            new Thread(() -> handleServerConsoleInput(out)).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientMessages(BufferedReader in, PrintWriter out) {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client message: " + message);

                if (message.equalsIgnoreCase("bye")) {
                    out.println("Goodbye, client!");
                    System.out.println("Client disconnected.........");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleServerConsoleInput(PrintWriter out) {
        try {
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
            String messageOut;
            while ((messageOut = serverInput.readLine()) != null) {
                out.println(messageOut);

                // Break the loop if the user types "bye"
                if (messageOut.equalsIgnoreCase("bye")) {
                    out.println("Goodbye, server!");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
