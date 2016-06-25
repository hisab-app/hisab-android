package io.github.zkhan93.hisab.model;

import java.util.List;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class Group {
    List<User> members;
    String name;
    String id;
    User moderator;

    public Group() {
    }

    public Group(List<User> members, String name, String id, User moderator) {
        this.members = members;
        this.name = name;
        this.id = id;
        this.moderator = moderator;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
    }
}
