package app.neont.api;

public class MessageModel {
    private String messageId, senderId, receiverId, text, status;
    private long timestamp;

    public MessageModel() {}
    public MessageModel(String id, String sender, String receiver, String text, long ts, String status) {
        this.messageId = id; this.senderId = sender; this.receiverId = receiver;
        this.text = text; this.timestamp = ts; this.status = status;
    }
    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}
