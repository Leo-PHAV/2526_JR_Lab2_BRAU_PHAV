package TP5;

import javax.net.ssl.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;

public class SSLClientHybridExo5 {

    private SSLSocket socket;
    private InputStream serverInput;
    private OutputStream serverOutput;

    public static void main(String[] args) {
        new SSLClientHybridExo5().startClient();
    }

    public void startClient(){
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers(){ return null; }
                    public void checkClientTrusted(X509Certificate[] c,String a){}
                    public void checkServerTrusted(X509Certificate[] c,String a){}
                }
            };
            ctx.init(null, trustAll, null);
            SSLSocketFactory sf = ctx.getSocketFactory();
            socket = (SSLSocket) sf.createSocket("localhost", 8443);
            socket.startHandshake();

            serverInput = socket.getInputStream();
            serverOutput = socket.getOutputStream();
            System.out.println("[CLIENT] Connected!");

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Type your commands/messages (exit to quit):");

            while(true){
                String line = console.readLine();
                if(line.equalsIgnoreCase("exit")) break;

                ChatMessage msg = parseCommand(line);
                if(msg != null) sendMessage(msg);
            }

        } catch(Exception e){ e.printStackTrace(); }
        finally { disconnect(); }
    }

    private ChatMessage parseCommand(String line){
        try {
            if(line.startsWith("/join ")){
                return new ChatMessage(MessageType.JOIN_ROOM_REQUEST, (byte)1,
                        System.currentTimeMillis(), "User1","",line.substring(6),"");
            }
            else if(line.startsWith("/leave ")){
                return new ChatMessage(MessageType.LEAVE_ROOM_REQUEST, (byte)1,
                        System.currentTimeMillis(), "User1","",line.substring(7),"");
            }
            else if(line.startsWith("/msg ")){
                String[] parts = line.split(" ",3);
                return new ChatMessage(MessageType.PRIVATE_MESSAGE,(byte)1,
                        System.currentTimeMillis(),"User1","",parts[1],parts[2]);
            }
            else{
                return new ChatMessage(MessageType.TEXT_MESSAGE,(byte)1,
                        System.currentTimeMillis(),"User1","","general",line);
            }
        } catch(Exception e){ return null; }
    }

    private void sendMessage(ChatMessage msg){
        try{
            byte[] body = msg.serialize();
            ByteBuffer buf = ByteBuffer.allocate(8+body.length);
            buf.put((byte)msg.getType().ordinal());
            buf.put((byte)0); buf.putShort((short)0);
            buf.putInt(body.length);
            buf.put(body);
            serverOutput.write(buf.array());
            serverOutput.flush();
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void disconnect(){
        try{ if(socket!=null) socket.close(); System.out.println("[CLIENT] Disconnected."); }
        catch(Exception ignored){}
    }
}
