package TP5;

public class ProtocolParser {
    public ChatMessage parse(byte[] data){
        return ChatMessage.deserialize(data);
    }
}
