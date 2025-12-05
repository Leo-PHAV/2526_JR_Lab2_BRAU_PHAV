package TP5;

import java.util.HashSet;
import java.util.Set;

public class ChatRoom {

    private String name;
    private Set<ClientSession> users = new HashSet<>();

    public ChatRoom(String n){ this.name=n; }

    public void addUser(ClientSession s){ users.add(s); }
    public void removeUser(ClientSession s){ users.remove(s); }
    public Set<ClientSession> getUsers(){ return users; }
}
