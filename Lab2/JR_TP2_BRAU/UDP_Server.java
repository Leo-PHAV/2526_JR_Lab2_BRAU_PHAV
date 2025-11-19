package TP2;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;


public class UDP_Server{
    private DatagramSocket datagramSocket;// Doit tj être ouvert pour recevoir ou envoyer contrairement a datagrampacket qui n'a pas besoin d'être dans le constructeur pc il ne sert qui quand on a reçu un packet
    private byte[] buffer = new byte[256];


    public UDP_Server(DatagramSocket datagramSocket){
        this.datagramSocket = datagramSocket;
    }

    public void receiveThenSend(){
        while (true){
            try{
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                InetAddress inetAddress = datagramPacket.getAddress();//récupérer l’adresse IP de l’expéditeur du paquet UDP reçu.
                int port = datagramPacket.getPort();
                String messagefromClient = new String(datagramPacket.getData(),datagramPacket.getOffset(),datagramPacket.getLength());
                System.out.println("Message from client : "+ messagefromClient + "\n");
                datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress,port);
                datagramSocket.send(datagramPacket);
            }catch (IOException  e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = new DatagramSocket(8080);// port = 8080
        UDP_Server  server = new UDP_Server(datagramSocket);
        server.receiveThenSend();
    }
}
