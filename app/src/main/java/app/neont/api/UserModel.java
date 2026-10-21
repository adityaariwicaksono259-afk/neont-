package app.neont.api;
public class UserModel {
    private String uid, name, username;
    public UserModel() {}
    public UserModel(String uid, String name, String username) { this.uid = uid; this.name = name; this.username = username; }
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getUsername() { return username; }
}
