package TP5;

import javax.net.ssl.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class SSLClientHybrid {

    private SSLSocket socket;
    private String host;
    private int port;
    private boolean trustAllCerts; // pour testing mode uniquement

    private InputStream serverInput;
    private OutputStream serverOutput;

    public SSLClientHybrid(String host, int port, boolean trustAllCerts) {
        this.host = host;
        this.port = port;
        this.trustAllCerts = trustAllCerts;
    }

    // Create SSLContext
    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        if (trustAllCerts) {
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            sslContext.init(null, trustAll, null);
            System.out.println("[CLIENT] TEST MODE: trusting all certificates.");
        } else {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); 
            sslContext.init(null, tmf.getTrustManagers(), null);
            System.out.println("[CLIENT] PRODUCTION MODE: validating certificates.");
        }
        return sslContext;
    }

    public void connect() {
        try {
            SSLContext sslContext = createSSLContext();
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            socket = (SSLSocket) ssf.createSocket(host, port);
            socket.startHandshake();

            serverInput = socket.getInputStream();
            serverOutput = socket.getOutputStream();

            System.out.println("[CLIENT] Connected to server with SSL/TLS");

        } catch (Exception e) {
            System.err.println("[ERROR] Connection failed: " + e.getMessage());
        }
    }

    public void sendMessage(byte type, String jsonBody) throws IOException {
        byte[] bodyBytes = jsonBody.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(8 + bodyBytes.length);
        buffer.put(type);               // 1 byte type
        buffer.put((byte) 0);           // flags
        buffer.putShort((short) 0);     // reserved
        buffer.putInt(bodyBytes.length);// length
        buffer.put(bodyBytes);
        serverOutput.write(buffer.array());
        serverOutput.flush();
    }

    public String receiveMessage() throws IOException {
        byte[] header = serverInput.readNBytes(8);
        if (header.length < 8) return null;

        int length = ByteBuffer.wrap(header, 4, 4).getInt();
        byte[] bodyBytes = serverInput.readNBytes(length);
        return new String(bodyBytes);
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
            System.out.println("[CLIENT] Disconnected.");
        } catch (IOException e) {
            System.err.println("[ERROR] Disconnect failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SSLClientHybrid client = new SSLClientHybrid("localhost", 8443, true);
        client.connect();

        if (client.socket == null) return;

        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Type your messages (type 'exit' to quit):");

            String msg;
            while ((msg = console.readLine()) != null) {
                if (msg.equalsIgnoreCase("exit")) break;

                client.sendMessage((byte) 5, "{\"message\":\"" + msg + "\"}");
                String response = client.receiveMessage();
                if (response != null) {
                    System.out.println("Server replied: " + response);
                }
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Communication error: " + e.getMessage());
        } finally {
            client.disconnect();
        }
    }
}
