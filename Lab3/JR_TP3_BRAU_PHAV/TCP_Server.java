package TP3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.ServerSocket;

public class TCP_Server{
    public static void main(String[] args) throws IOException{
        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        
        ServerSocket serverSocket = new ServerSocket(8080); 
       

        while(true){
            try{
                socket = serverSocket.accept();
                inputStreamReader = new InputStreamReader(socket.getInputStream());// recupere le flux venant du serveur 
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());//flux pour envoyer au serveur
                bufferedReader = new BufferedReader(inputStreamReader);// convertit les bytes en texte
                bufferedWriter = new BufferedWriter(outputStreamWriter);// convertit le texte en bytes


                while (true){
                    String msgfromClient = bufferedReader.readLine();

                    System.out.println("Client : " + msgfromClient);

                    bufferedWriter.write("MSG Received");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    if (msgfromClient.equalsIgnoreCase("OK BYE")){
                        break ; 
                    }
                }

            } catch (IOException e){
                e.printStackTrace();
            } finally {
                try{
                    if (serverSocket != null){serverSocket.close();}
                }catch (IOException e ){e.printStackTrace();}
            }
        }
    }
}