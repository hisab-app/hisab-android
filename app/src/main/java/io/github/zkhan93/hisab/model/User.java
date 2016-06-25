package io.github.zkhan93.hisab.model;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class User {
    String name;
    String email;
    String id;

    public User() {
    }

    public User(String name, String email, String id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
