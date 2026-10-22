package app.neont.api;

import java.util.Map;

public class GroupModel {
    private String groupId;
    private String title;
    private String description;
    private String photoUrl;
    private String type; // "group" atau "channel"
    private String adminId; // Admin utama
    private Map<String, Boolean> members; // Tiru ChatParticipant
    private Map<String, Boolean> admins;

    public GroupModel() {}
    public GroupModel(String groupId, String title, String description, String photoUrl, String type, String adminId, Map<String, Boolean> members, Map<String, Boolean> admins) {
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.type = type;
        this.adminId = adminId;
        this.members = members;
        this.admins = admins;
    }
    public String getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photoUrl; }
    public String getType() { return type; }
    public String getAdminId() { return adminId; }
    public Map<String, Boolean> getMembers() { return members; }
    public Map<String, Boolean> getAdmins() { return admins; }
}
