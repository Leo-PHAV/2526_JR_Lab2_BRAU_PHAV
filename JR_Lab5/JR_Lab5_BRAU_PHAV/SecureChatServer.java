package TP5;

import javax.net.ssl.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecureChatServer {

    private SSLServerSocket serverSocket;
    private Map<String, ClientSession> activeSessions = new ConcurrentHashMap<>();
    private Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();
    private ProtocolParser messageParser = new ProtocolParser();
    private boolean running = true;

    public SecureChatServer(int port, String keystore, String password) throws Exception {
        SSLContext ctx = createSSLContext(keystore, password);
        serverSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(port);
        serverSocket.setEnabledProtocols(new String[]{"TLSv1.2","TLSv1.3"});
        System.out.println("[SERVER] Running on port " + port);
    }

    private SSLContext createSSLContext(String path,String pwd) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(path), pwd.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, pwd.toCharArray());

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        return ctx;
    }

    public void start() {
        while(running) {
            try {
                SSLSocket socket = (SSLSocket)serverSocket.accept();
                System.out.println("[SERVER] New client connected");
                new Thread(() -> handleClient(socket)).start();
            } catch(Exception e){ System.err.println("[ERR] Accept: "+e); }
        }
    }

    private void handleClient(SSLSocket socket) {
        try {
            socket.startHandshake();
            InputStream in = socket.getInputStream();
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while(true){
                byte[] header = in.readNBytes(8);
                if(header.length < 8) break;

                int length = ByteBuffer.wrap(header,4,4).getInt();
                byte[] body = in.readNBytes(length);

                ChatMessage msg = messageParser.parse(body);
                if(msg != null) handleProtocolMessage(msg, out);
            }

        } catch(Exception e){ System.err.println("[ERR CLIENT] "+e); }
    }

    private void handleProtocolMessage(ChatMessage msg, DataOutputStream out){
        switch(msg.getType()){
            case LOGIN_REQUEST -> processLogin(msg, out);
            case JOIN_ROOM_REQUEST -> processJoinRoom(msg, out);
            case LEAVE_ROOM_REQUEST -> processLeaveRoom(msg, out);
            case TEXT_MESSAGE -> processTextMessage(msg);
            case PRIVATE_MESSAGE -> processPrivateMessage(msg);
            default -> sendError(out,"Unknown protocol message");
        }
    }

    private void processLogin(ChatMessage msg, DataOutputStream out){
        if(activeSessions.containsKey(msg.getSender())){
            sendError(out,"User already connected");
            return;
        }
        activeSessions.put(msg.getSender(), new ClientSession(msg.getSender(), out));
        System.out.println("[LOGIN] "+msg.getSender()+" logged in");
    }

    private void processJoinRoom(ChatMessage msg, DataOutputStream out){
        ChatRoom room = chatRooms.computeIfAbsent(msg.getRoom(), ChatRoom::new);
        ClientSession user = activeSessions.get(msg.getSender());
        if(user != null) {
            room.addUser(user);
            System.out.println("[ROOM] "+msg.getSender()+" joined "+msg.getRoom());
        }
    }

    private void processLeaveRoom(ChatMessage msg, DataOutputStream out){
        ChatRoom room = chatRooms.get(msg.getRoom());
        ClientSession user = activeSessions.get(msg.getSender());
        if(room != null && user != null) {
            room.removeUser(user);
            System.out.println("[ROOM] "+msg.getSender()+" left "+msg.getRoom());
        }
    }

    private void processTextMessage(ChatMessage msg){
        ChatRoom room = chatRooms.get(msg.getRoom());
        if(room == null) return;
        for(ClientSession c : room.getUsers()){
            c.send(msg);
        }
    }

    private void processPrivateMessage(ChatMessage msg){
        ClientSession dest = activeSessions.get(msg.getRecipient());
        if(dest!=null) dest.send(msg);
    }

    private void sendError(DataOutputStream out,String err){
        try{
            ChatMessage msg = new ChatMessage(MessageType.ERROR_RESPONSE,(byte)1,
                    System.currentTimeMillis(),"server","", "",err);
            out.write(msg.serialize());
            out.flush();
        } catch(Exception ignored){}
    }

    public static void main(String[] a) throws Exception {
        SecureChatServer s = new SecureChatServer(8443,"server.jks","password");
        s.start();
    }
}
