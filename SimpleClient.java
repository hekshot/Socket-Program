import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SimpleClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java SimpleClient <server-address> <server-port>");
            return;
        }

        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        Socket socket = null;
        PrintWriter out = null;

        try {
            socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server: " + serverAddress);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String encodedKey = in.readLine();
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "RC4");

            Cipher rc4Cipher = Cipher.getInstance("RC4");
            rc4Cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("encrypted: " + response);
                        System.out.println("Server message: " + decryptMessage(response, rc4Cipher));
                        if (response.equalsIgnoreCase("Goodbye, client!")) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = userInput.readLine()) != null) {
                out.println(encryptMessage(message, rc4Cipher));

                if (message.equalsIgnoreCase("bye")) {
                    break;
                }
            }
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String encryptMessage(String message, Cipher cipher) throws IOException {
        try {
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException("Error encrypting message", e);
        }

    }

    private static String decryptMessage(String encryptedMessage, Cipher cipher) throws IOException {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException("Error decrypting message", e);
        }

    }
}
