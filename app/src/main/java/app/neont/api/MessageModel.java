package app.neont.api;

public class MessageModel {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String text;
    private long timestamp;
    private String status; // "sent", "delivered", "read"

    public MessageModel() {}

    public MessageModel(String messageId, String senderId, String receiverId, String text, long timestamp, String status) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}
