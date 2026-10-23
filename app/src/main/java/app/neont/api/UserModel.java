package app.neont.api;
public class UserModel {
    private String uid, name, username, preview;
    public UserModel() {}
    public UserModel(String uid, String name, String username) { this.uid = uid; this.name = name; this.username = username; }
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }
}
