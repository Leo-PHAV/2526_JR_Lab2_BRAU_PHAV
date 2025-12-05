package TP5;

import javax.net.ssl.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.KeyStore;

public class SSLTCPServerHybrid {

    private int port;
    private SSLServerSocket serverSocket;
    private boolean isRunning = false;

    // Constructor
    public SSLTCPServerHybrid(int port, String keystorePath, String password) {
        this.port = port;
        try {
            SSLContext context = createSSLContext(keystorePath, password);
            SSLServerSocketFactory ssf = context.getServerSocketFactory();
            serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            System.out.println("[SERVER] SSL Server initialized on port " + port);
        } catch (Exception e) {
            System.err.println("[ERROR] Server initialization failed: " + e.getMessage());
        }
    }

    private SSLContext createSSLContext(String keystorePath, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystorePath), password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, password.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            return sslContext;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext: " + e.getMessage());
        }
    }

    public void launch() {
        System.out.println("[SERVER] Waiting for SSL clients...");
        isRunning = true;

        while (isRunning) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("[SERVER] Client connected from " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();

            } catch (IOException e) {
                if (isRunning)
                    System.err.println("[ERROR] Connection issue: " + e.getMessage());
            }
        }
    }

    private void handleClient(SSLSocket clientSocket) {
        try {
            clientSocket.startHandshake();
            System.out.println("[SERVER] Handshake successful with " + clientSocket.getInetAddress());

            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            while (true) {
                byte[] header = input.readNBytes(8);
                if (header.length < 8) break; 
                byte type = header[0];
                int length = ByteBuffer.wrap(header, 4, 4).getInt();

                byte[] bodyBytes = input.readNBytes(length);
                String jsonBody = new String(bodyBytes);

                System.out.println("[SERVER] Received Type: " + type + " Body: " + jsonBody);

                sendMessage(output, type, jsonBody);
            }

            clientSocket.close();
            System.out.println("[SERVER] Client disconnected.");

        } catch (Exception e) {
            System.err.println("[ERROR] Client handling failed: " + e.getMessage());
        }
    }

    private void sendMessage(OutputStream out, byte type, String jsonBody) throws IOException {
        byte[] bodyBytes = jsonBody.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(8 + bodyBytes.length);
        buffer.put(type);               // 1 byte type
        buffer.put((byte) 0);           // 1 byte flags
        buffer.putShort((short) 0);     // 2 bytes reserved
        buffer.putInt(bodyBytes.length);// 4 bytes length
        buffer.put(bodyBytes);
        out.write(buffer.array());
        out.flush();
    }

    // Shutdown server
    public void shutdown() {
        try {
            isRunning = false;
            serverSocket.close();
            System.out.println("[SERVER] Shutdown complete.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to shutdown server: " + e.getMessage());
        }
    }

    // MAIN
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java SSLTCPServer <keystore-password>");
            return;
        }
        SSLTCPServerHybrid server = new SSLTCPServerHybrid(8443, "server.jks", args[0]);
        server.launch();
    }
}
