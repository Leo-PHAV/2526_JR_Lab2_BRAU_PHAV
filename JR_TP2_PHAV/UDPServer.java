import java.net.*;

public class UDPServer{
    private int port;
    public static final int DEFAULT_PORT = 8080;

    public UDPServer(){
        this.port = DEFAULT_PORT;
    }

    public UDPServer(int port){
        this.port = port;
    }

    public void launch(){
        try {
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println(toString());
            byte[] message = new byte[1024];
            DatagramPacket packet = new DatagramPacket(message, message.length);
            
            while(true){
                packet.setLength(message.length);
                socket.receive(packet);
                String str = new String(packet.getData(), packet.getOffset(), packet.getLength(), java.nio.charset.StandardCharsets.UTF_8);
                System.out.println("Received: " + str);
            }
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String toString() {
            return "UDPServer listening on port: " + port;
        }

    public static void main(String[] args){
        UDPServer server = null;
        if (args.length > 0){
            try{
                int port = Integer.parseInt(args[0]);
                server = new UDPServer(port);
            } catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        } else{
            server = new UDPServer();
        }
        server.launch();
    }
}