
package TP5;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ChatMessage {

    private MessageType type;
    private byte protocolVersion;
    private long timestamp;
    private String sender;
    private String recipient;  // pour messages privés
    private String room;       // pour chat rooms
    private String content;

    public ChatMessage(MessageType type, byte protocolVersion, long timestamp,
                       String sender, String recipient, String room, String content) {
        this.type = type;
        this.protocolVersion = protocolVersion;
        this.timestamp = timestamp;
        this.sender = sender;
        this.recipient = recipient;
        this.room = room;
        this.content = content;
    }

    public ChatMessage() {}

    // ---------- Sérialisation en byte array ----------
    public byte[] serialize() {
        byte[] senderBytes = (sender != null ? sender : "").getBytes(StandardCharsets.UTF_8);
        byte[] recipientBytes = (recipient != null ? recipient : "").getBytes(StandardCharsets.UTF_8);
        byte[] roomBytes = (room != null ? room : "").getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = (content != null ? content : "").getBytes(StandardCharsets.UTF_8);

        int totalLength = 1 + 1 + 8 + 4*4 + senderBytes.length + recipientBytes.length + roomBytes.length + contentBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        buffer.put((byte) type.ordinal());
        buffer.put(protocolVersion);
        buffer.putLong(timestamp);

        buffer.putInt(senderBytes.length);
        buffer.putInt(recipientBytes.length);
        buffer.putInt(roomBytes.length);
        buffer.putInt(contentBytes.length);

        buffer.put(senderBytes);
        buffer.put(recipientBytes);
        buffer.put(roomBytes);
        buffer.put(contentBytes);

        return buffer.array();
    }

    // ---------- Désérialisation ----------
    public static ChatMessage deserialize(byte[] data) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            ChatMessage msg = new ChatMessage();

            int typeOrdinal = Byte.toUnsignedInt(buffer.get());
            msg.type = MessageType.values()[typeOrdinal];
            msg.protocolVersion = buffer.get();
            msg.timestamp = buffer.getLong();

            int senderLen = buffer.getInt();
            int recipientLen = buffer.getInt();
            int roomLen = buffer.getInt();
            int contentLen = buffer.getInt();

            byte[] senderBytes = new byte[senderLen];
            byte[] recipientBytes = new byte[recipientLen];
            byte[] roomBytes = new byte[roomLen];
            byte[] contentBytes = new byte[contentLen];

            buffer.get(senderBytes);
            buffer.get(recipientBytes);
            buffer.get(roomBytes);
            buffer.get(contentBytes);

            msg.sender = new String(senderBytes, StandardCharsets.UTF_8);
            msg.recipient = new String(recipientBytes, StandardCharsets.UTF_8);
            msg.room = new String(roomBytes, StandardCharsets.UTF_8);
            msg.content = new String(contentBytes, StandardCharsets.UTF_8);

            return msg;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------- Getters ----------
    public MessageType getType() { return type; }
    public byte getProtocolVersion() { return protocolVersion; }
    public long getTimestamp() { return timestamp; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getRoom() { return room; }
    public String getContent() { return content; }
}
