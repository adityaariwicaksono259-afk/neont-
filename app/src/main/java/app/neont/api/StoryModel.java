package app.neont.api;

import java.util.List;

public class StoryModel {
    private String uid;
    private String name;
    private String imageUrl;
    private long timestamp;
    private String privacy; // "everyone", "contacts", "close_friends", "selected"
    private List<String> allowedUsers; // Khusus "selected"

    public StoryModel() {}

    public StoryModel(String uid, String name, String imageUrl, long timestamp, String privacy, List<String> allowedUsers) {
        this.uid = uid;
        this.name = name;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.privacy = privacy;
        this.allowedUsers = allowedUsers;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }
    public String getPrivacy() { return privacy; }
    public List<String> getAllowedUsers() { return allowedUsers; }
}
