import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SimpleServer {

    private static boolean chatMode = false;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SimpleServer <port>");
            return;
        }

        int portNumber = Integer.parseInt(args[0]);

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server is listening on port " + portNumber);

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            SecretKey secretKey = generateSecretKey();

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Handle client messages and initial options
            new Thread(() -> handleClientMessages(in, out)).start();

            // Start a separate thread to handle messages from the server console
            new Thread(() -> handleServerConsoleInput(out)).start();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing the server socket: " + e.getMessage());
                }
            }
        }
    }

    private static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("RC4");
        return keyGen.generateKey();
    }

    private static String encryptMessage(String message, Cipher rc4Cipher) throws IOException {
        try {
            byte[] encryptedBytes = rc4Cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException("Error encrypting message", e);
        }
    }

    private static String decryptMessage(String encryptedMessage, Cipher rc4Cipher) throws IOException {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = rc4Cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException("Error decrypting message", e);
        }
    }

    private static void handleClientMessages(BufferedReader in, PrintWriter out, Cipher rc4Cipher) {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client message: " + message);

                if (message.equalsIgnoreCase("bye")) {
                    out.println("Goodbye, client!");
                    System.out.println("Client disconnected.........");
                    return;
                }
            }
        } catch (IOException e) {
            // Handle client disconnect gracefully
            System.out.println("Client disconnected.........");
        }
    }

    private static void handleServerConsoleInput(PrintWriter out, Cipher rc4Cipher) {
        try {
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
            String messageOut;
            while ((messageOut = serverInput.readLine()) != null) {
                out.println(messageOut);

                if (messageOut.equalsIgnoreCase("bye")) {
                    out.println("Goodbye, server!");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling server console input: " + e.getMessage());
        }
    }
}
