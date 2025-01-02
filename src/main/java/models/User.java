package models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final String name;
    private final String uuid;
    private final List<Link> links;

    public User(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
        this.links = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void addLink(Link link) {
        links.add(link);
    }
}
