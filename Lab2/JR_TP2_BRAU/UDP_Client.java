package TP2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.net.UnknownHostException;


public class UDP_Client {
    private DatagramSocket datagramSocket;
    private InetAddress inetAdress;
    private byte[] buffer ; 
    
    public UDP_Client(DatagramSocket datagramSocket,InetAddress inetAdress){
        this.datagramSocket = datagramSocket;
        this.inetAdress = inetAdress;
    }

    public void sendThenReceive(){
        int idx = 0;
         Scanner scanner = new Scanner(System.in);
         while(true){
            try{
                String messageToSend = "Seq = " + idx + " | "+ scanner.nextLine();
                buffer = messageToSend.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length,inetAdress,8080);
                datagramSocket.send(datagramPacket);
                datagramSocket.receive(datagramPacket);
                String messageFromServer = new String(datagramPacket.getData(),0,datagramPacket.getLength());
                System.out.println("Seq = " + idx + " | The server say you say " + messageFromServer );
                idx ++;
            } catch(IOException e){
                    e.printStackTrace();
                    break;
            }         
        }
    }

    public static void main(String[] args) throws SocketException, UnknownHostException{
        DatagramSocket datagramSocket =  new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName("localhost");
        UDP_Client client = new UDP_Client(datagramSocket,inetAddress);
        System.out.println("Send datagram packets to a server UDP ");
        client.sendThenReceive();
    }
}  
