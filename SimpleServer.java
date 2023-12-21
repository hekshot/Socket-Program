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

            out.println(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

            Cipher rc4Cipher = Cipher.getInstance("RC4");
            rc4Cipher.init(Cipher.DECRYPT_MODE, secretKey);

            new Thread(() -> handleClientMessages(in, out, rc4Cipher)).start();

            new Thread(() -> handleServerConsoleInput(out, rc4Cipher)).start();

        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
                System.out.println("encrypted" + message);
                System.out.println("Client message: " + decryptMessage(message, rc4Cipher));

                if (message.equalsIgnoreCase("bye")) {
                    out.println(encryptMessage("Goodbye, client!", rc4Cipher));
                    System.out.println("Client disconnected.........");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.........");
        }
    }

    private static void handleServerConsoleInput(PrintWriter out, Cipher rc4Cipher) {
        try {
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
            String messageOut;
            while ((messageOut = serverInput.readLine()) != null) {
                out.println(encryptMessage(messageOut, rc4Cipher));

                if (messageOut.equalsIgnoreCase("bye")) {
                    out.println(encryptMessage("Goodbye, server!", rc4Cipher));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
