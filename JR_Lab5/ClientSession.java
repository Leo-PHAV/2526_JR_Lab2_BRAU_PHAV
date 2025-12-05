package TP5;

import java.io.DataOutputStream;

public class ClientSession {
    private String username;
    private DataOutputStream out;

    public ClientSession(String user, DataOutputStream out){
        this.username=user;
        this.out=out;
    }

    public void send(ChatMessage msg){
        try{
            out.write(msg.serialize());
            out.flush();
        }catch(Exception ignored){}
    }

    public String getUser(){ return username; }
}
