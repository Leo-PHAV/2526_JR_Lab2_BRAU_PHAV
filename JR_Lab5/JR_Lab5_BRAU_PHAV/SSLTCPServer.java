package TP5;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class SSLTCPServer {

    private int port;
    private SSLServerSocket serverSocket;
    private boolean isRunning = false;

    // Constructor
    public SSLTCPServer(int port, String keystorePath, String password) {
        this.port = port;
        try {
            SSLContext context = createSSLContext(keystorePath, password);

            SSLServerSocketFactory ssf = context.getServerSocketFactory();
            serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

            serverSocket.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});

            System.out.println("[SERVER] SSL Server initialized on port " + port);
        } catch (Exception e) {
            System.err.println("[ERROR] Server initialization failed: " + e.getMessage());
        }
    }

    // Step 1 - Create SSL Context
    private SSLContext createSSLContext(String keystorePath, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");  // Instruction imposée
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

    // methode launch
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


    // methode handleClient
    private void handleClient(SSLSocket clientSocket) {
        try {
            clientSocket.startHandshake(); 
            System.out.println("[SERVER] Handshake successful with " + clientSocket.getInetAddress());

            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            while ((message = input.readLine()) != null) {
                System.out.println("[ECHO] " + message);
                output.println("Echo: " + message);  
            }

            clientSocket.close();
            System.out.println("[SERVER] Client disconnected.");

        } catch (Exception e) {
            System.err.println("[ERROR] Client handling failed: " + e.getMessage());
        }
    }

    // methode shutdown
    public void shutdown() {
        try {
            isRunning = false;
            serverSocket.close();
            System.out.println("[SERVER] Shutdown complete.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to shutdown server: " + e.getMessage());
        }
    }

    // ---------- MAIN TEST ---------- //
    public static void main(String[] args) {
        SSLTCPServer server = new SSLTCPServer(8443, "server.jks", args[0]);
        server.launch();
    }
}
