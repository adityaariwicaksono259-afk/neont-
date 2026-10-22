package app.neont.api;

import java.util.Map;

public class MessageModel {
    private String messageId, senderId, receiverId, text, status, replyToMsgId, mediaUrl;
    private long timestamp;
    private Map<String, Map<String, Boolean>> reactions;

    public MessageModel() {}
    public MessageModel(String id, String sender, String receiver, String text, long ts, String status, String replyToMsgId, String mediaUrl, Map<String, Map<String, Boolean>> reactions) {
        this.messageId = id; this.senderId = sender; this.receiverId = receiver;
        this.text = text; this.timestamp = ts; this.status = status;
        this.replyToMsgId = replyToMsgId; this.mediaUrl = mediaUrl; this.reactions = reactions;
    }
    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getReplyToMsgId() { return replyToMsgId; }
    public String getMediaUrl() { return mediaUrl; }
    public Map<String, Map<String, Boolean>> getReactions() { return reactions; }
}
