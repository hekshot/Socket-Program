import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {

    private static boolean chatMode = false;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SimpleServer <port>");
            return;
        }

        int portNumber = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server is listening on port " + portNumber);

            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
    
                // Create input and output streams for communication
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    
                // Reset chatMode when a new client connects
                chatMode = false;
    
                // Handle client messages and initial options in a separate thread
                new Thread(() -> handleClientMessages(in, out)).start(); 
                new Thread(() -> handleServerConsoleInput(out)).start();
            }
        } catch (IOException e) {
            System.err.println("Error setting up the server: " + e.getMessage());
        }
    }


    private static void handleClientMessages(BufferedReader in, PrintWriter out) {
        try {
            while (true) {
                // Send initial options to the client
                out.println("Welcome to the server! Choose an option: /CHAT or /CALC");
    
                // Read the client's choice
                String choice = in.readLine();
    
                // Process the client's choice
                if (choice != null) {
                    if (choice.equalsIgnoreCase("/CHAT")) {
                        // Set the chatMode to true
                        chatMode = true;
                        out.println("Chat mode activated. Type '/EXIT' to exit chat mode.");
    
                        // Process chat messages
                        processChatMessages(in, out);
                    } else if (choice.equalsIgnoreCase("/CALC")) {
                        // Set the chatMode to false
                        chatMode = false;
                        out.println(
                                "Calculator mode activated. You can now perform calculations. Type '/EXIT' to exit and '/HELP' for help.");
    
                        // Process calculator messages
                        processCalcMessages(in, out);
                    } else if (choice.equalsIgnoreCase("bye")) {
                        out.println("Goodbye!");
                        break;
                    } else {
                        // Invalid choice, notify the client
                        out.println("Invalid choice. Please choose /CHAT or /CALC");
                    }
                } else {
                    // Client disconnected before making a choice
                    System.out.println("Client disconnected.........");
                    break;
                }
            }
        } catch (IOException e) {
            // Handle client disconnect
            if (e.getMessage() != null && e.getMessage().equalsIgnoreCase("Socket closed")) {
                System.out.println("Client disconnected.........");
            } else {
                System.err.println("Error handling client messages: " + e.getMessage());
            }
        }
    }
    private static void processChatMessages(BufferedReader in, PrintWriter out) throws IOException {
        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Client message: " + message);

            // Process the request based on the selected mode
            String response;
            if (chatMode) {
                // Handle chat messages
                response = processChatRequest(message);
            } else {
                // Process calculator requests
                response = processCalcRequest(message);
            }

            out.println(response);

            // Check for /EXIT to exit the mode
            if (message.equalsIgnoreCase("/EXIT")) {
                out.println("Exiting chat mode. Choose an option: /CHAT or /CALC");
                return;
            }
        }
    }

    private static void processCalcMessages(BufferedReader in, PrintWriter out) throws IOException {
        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Client message: " + message);

            // Process the request based on the selected mode
            String response;
            if (chatMode) {
                // Handle chat messages
                response = processChatRequest(message);
            } else {
                // Process calculator requests
                response = processCalcRequest(message);
            }

            out.println(response);

            // Check for /EXIT to exit the mode
            if (message.equalsIgnoreCase("/EXIT")) {
                out.println("Exiting calculator mode. Choose an option: /CHAT or /CALC");
                return;
            }
        }
    }

    private static String processChatRequest(String message) {
        // Handle chat mode requests
        return "";
    }

    private static String processCalcRequest(String message) {
        // Handle calculator mode requests
        return processRequest(message);
    }

    private static String processRequest(String request) {
        String[] tokens = request.split(" ");
        if (tokens.length >= 1) {
            String operation = tokens[0];
            if (operation.equalsIgnoreCase("/HELP")) {
                // Special case: "/help" without additional arguments
                return "Available operations: /ADD, /SUB, /MUL, /DIV, /FACT, /SQUARE, /CHAT, /CALC\"";
            } else if (operation.equalsIgnoreCase("/CHAT")) {
                // Switch to chat mode
                chatMode = true;
                return "Chat mode activated. Type 'bye' to exit chat mode.";
            } else if (operation.equalsIgnoreCase("/CALC")) {
                // Switch back to calculator mode
                chatMode = false;
                return "Calculator mode activated. You can now perform calculations.";
            }

            else if (tokens.length >= 2) {
                String[] operands = tokens[1].split("\\|");
                int init = 0;
                switch (operation) {

                    case "/ADD":
                        int sum = init;
                        // Add the additional operands to the initialized value
                        for (String operand : operands) {
                            sum += Integer.parseInt(operand);
                        }
                        return "Result of addition: " + sum;

                    case "/SUB":
                        int subtraction = Integer.parseInt(operands[0]);
                        for (int i = 1; i < operands.length; i++) {
                            subtraction -= Integer.parseInt(operands[i]);
                        }
                        return String.valueOf(subtraction);

                    case "/MUL":
                        int product = 1;
                        for (String operand : operands) {
                            product *= Integer.parseInt(operand);
                        }
                        return String.valueOf(product);

                    case "/DIV":
                        if (operands.length == 0) {
                            return "Invalid operation";
                        }
                        double divisionResult = Double.parseDouble(operands[0]);
                        for (int i = 1; i < operands.length; i++) {
                            double divisor = Double.parseDouble(operands[i]);
                            if (divisor != 0) {
                                divisionResult /= divisor;
                            } else {
                                return "Error: Division by zero";
                            }
                        }
                        return String.valueOf(divisionResult);

                    case "/FACT":
                        if (operands.length != 1) {
                            return "Invalid operation";
                        }
                        int number = Integer.parseInt(operands[0]);
                        int factorialResult = 1;
                        for (int i = 1; i <= number; i++) {
                            factorialResult *= i;
                        }
                        return String.valueOf(factorialResult);

                    case "/SQUARE":
                        if (operands.length != 1) {
                            return "Invalid operation";
                        }
                        int squareInput = Integer.parseInt(operands[0]);
                        int squareResult = squareInput * squareInput;
                        return String.valueOf(squareResult);

                    default:
                        return "Invalid operation";
                }
            }
        }
        return "Invalid request format";
    }

    private static void handleServerConsoleInput(PrintWriter out) {
        try {
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
            String messageOut;
            while ((messageOut = serverInput.readLine()) != null) {
                out.println("Server: " + messageOut);

                // Break the loop if the user types "bye"
                if (messageOut.equalsIgnoreCase("bye")) {
                    out.println("Goodbye!");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling server console input: " + e.getMessage());
        }
    }
}
