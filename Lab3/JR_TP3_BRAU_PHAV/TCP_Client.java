package TP3;

// https://www.youtube.com/watch?v=gchR3DpY-8Q Video i see to make the client and server TCP 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;



public class TCP_Client{


    public static void main(String[] args){

        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        try{
            socket = new Socket(host, port);
            inputStreamReader = new InputStreamReader(socket.getInputStream());// recupere le flux venant du serveur 
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());//flux pour envoyer au serveur
            bufferedReader = new BufferedReader(inputStreamReader);// convertit les bytes en texte
            bufferedWriter = new BufferedWriter(outputStreamWriter);// convertit le texte en bytes
            Scanner scanner = new Scanner(System.in);// permet d elire ce que tu lis dans la console

            while (true){
                String msgtoSend = scanner.nextLine();

                bufferedWriter.write(msgtoSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                System.out.println("Server : " + bufferedReader.readLine());
                if (msgtoSend.equalsIgnoreCase("BYE")){
                    break; 
                }


            } 
        }catch (IOException e) {
                e.printStackTrace();
        }finally{
            try{
                if (socket != null){socket.close();}
                if (bufferedReader != null) bufferedReader.close();
                if (bufferedWriter != null) bufferedWriter.close();
                if (inputStreamReader != null) inputStreamReader.close();
                if (outputStreamWriter != null) outputStreamWriter.close();
            } catch (IOException e ){
                e.printStackTrace();
            }
        
        }
    }
}