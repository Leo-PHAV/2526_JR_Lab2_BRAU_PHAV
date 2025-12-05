package TP5;

import javax.net.ssl.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class SSLTCPServerHybridExo5 {

    private int port;
    private SSLServerSocket serverSocket;
    private boolean isRunning = false;
    private final List<SSLSocket> clients = new ArrayList<>();

    public SSLTCPServerHybridExo5(int port, String keystorePath, String password) {
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
                synchronized (clients) { clients.add(clientSocket); }
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
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            while (true) {
                byte[] header = input.readNBytes(8);
                if (header.length < 8) break;

                int length = ByteBuffer.wrap(header, 4, 4).getInt();
                byte[] bodyBytes = input.readNBytes(length);

                ChatMessage msg = ChatMessage.deserialize(bodyBytes);
                if (msg == null) continue;

                System.out.println("[SERVER] Received " + msg.getType() +
                                   " from " + msg.getSender() +
                                   " content: " + msg.getContent());

                sendMessage(output, msg);

            }

            synchronized (clients) { clients.remove(clientSocket); }
            clientSocket.close();
            System.out.println("[SERVER] Client disconnected.");

        } catch (Exception e) {
            System.err.println("[ERROR] Client handling failed: " + e.getMessage());
        }
    }

    private void sendMessage(OutputStream out, ChatMessage msg) throws IOException {
        byte[] body = msg.serialize();
        ByteBuffer buffer = ByteBuffer.allocate(8 + body.length);
        buffer.put((byte) msg.getType().ordinal());
        buffer.put((byte) 0);
        buffer.putShort((short) 0);
        buffer.putInt(body.length);
        buffer.put(body);
        out.write(buffer.array());
        out.flush();
    }

    public void shutdown() {
        try {
            isRunning = false;
            serverSocket.close();
            System.out.println("[SERVER] Shutdown complete.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to shutdown server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TP5.SSLTCPServerHybridExo5 <keystore-password>");
            return;
        }
        SSLTCPServerHybridExo5 server = new SSLTCPServerHybridExo5(8443, "server.jks", args[0]);
        server.launch();
    }
}
