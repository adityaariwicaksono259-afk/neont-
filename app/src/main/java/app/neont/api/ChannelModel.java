package app.neont.api;

public class ChannelModel {
    private String id;
    private String name;
    private String description;
    private int followers;

    public ChannelModel() {}

    public ChannelModel(String id, String name, String description, int followers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.followers = followers;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getFollowers() { return followers; }
}
