package TP5;
import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class SSLClient {

    private SSLSocket socket;
    private String host;
    private int port;
    private boolean trustAllCerts; // pour testing mode uniquement

    private BufferedReader serverInput;
    private PrintWriter serverOutput;

    // ---------- Constructeur ----------
    public SSLClient(String host, int port, boolean trustAllCerts) {
        this.host = host;
        this.port = port;
        this.trustAllCerts = trustAllCerts;
    }

    // ---------- Créer SSLContext selon mode ----------
    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        if (trustAllCerts) {
            // Mode TEST : accepter tous les certificats
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
            // Mode PRODUCTION : valider les certificats
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // truststore par défaut
            sslContext.init(null, tmf.getTrustManagers(), null);
            System.out.println("[CLIENT] PRODUCTION MODE: validating certificates.");
        }

        return sslContext;
    }

    // ---------- Connecter au serveur ----------
    public void connect() {
        try {
            SSLContext sslContext = createSSLContext();
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            socket = (SSLSocket) ssf.createSocket(host, port);

            socket.startHandshake(); // handshake SSL/TLS
            System.out.println("[CLIENT] Connected to server with SSL/TLS");

            // initialiser flux
            serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOutput = new PrintWriter(socket.getOutputStream(), true);

        } catch (SSLHandshakeException e) {
            System.err.println("[ERROR] SSL Handshake failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Connection failed: " + e.getMessage());
        }
    }

    // ---------- Envoyer un message ----------
    public void sendMessage(String message) {
        if (serverOutput != null) {
            serverOutput.println(message);
        } else {
            System.err.println("[ERROR] Not connected.");
        }
    }

    // ---------- Recevoir réponse ----------
    public String receiveResponse() {
        if (serverInput != null) {
            try {
                return serverInput.readLine();
            } catch (IOException e) {
                System.err.println("[ERROR] Receiving response failed: " + e.getMessage());
            }
        }
        return null;
    }

    // ---------- Déconnecter ----------
    public void disconnect() {
        try {
            if (socket != null) socket.close();
            System.out.println("[CLIENT] Disconnected.");
        } catch (IOException e) {
            System.err.println("[ERROR] Disconnect failed: " + e.getMessage());
        }
    }

    // ---------- Envoyer et Recevoir  ----------
    public void startInteractiveSession() {
        if (socket == null) {
            System.err.println("[ERROR] Not connected to server.");
            return;
        }

        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Type your messages (type 'exit' to quit):");

            String msg;
            while ((msg = console.readLine()) != null) {
                if (msg.equalsIgnoreCase("exit")) break;

                sendMessage(msg); // envoyer au serveur
                String response = receiveResponse(); // lire réponse
                if (response != null) {
                    System.out.println("Server replied: " + response);
                }
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Communication error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    // ---------- MAIN ----------
    public static void main(String[] args) {
        // true = mode test (accept self-signed), false = production (validate certs)
        SSLClient client = new SSLClient("localhost", 8443, true);
        client.connect();
        client.startInteractiveSession();
    }
}
