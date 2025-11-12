import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

public class UDPClient{
    public static void main(String []args){
        try{
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            int port = (args.length > 0) ? Integer.parseInt(args[0]) : 8080;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            int sequenceNum = 1;
            System.out.println("Connected to " + address + ":" + port);

            while(true){
                String input = reader.readLine();
                if (input == null || input.isEmpty()) continue;
                String seqMessage = sequenceNum + " | " + input;
                byte[] message = seqMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
                socket.send(packet);
                sequenceNum++;
            }
            
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }   
    }    
}