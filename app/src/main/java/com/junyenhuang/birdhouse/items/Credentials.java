package com.junyenhuang.birdhouse.items;

public class Credentials {
    private boolean status = false;
    private String type;
    private int user_id = 0;
    private String username;
    private String password;

    public Credentials() {
        status = false;
        type = "";
        user_id = 0;
        username = "";
        password = "";
    }

    public void setStatus(boolean value) {
        status = value;
    }

    public boolean getStatus() {
        return status;
    }

    public void setCredentialType(String type) {
        this.type = type;
    }

    public String getCredentialType() {
        return type;
    }

    public void setUserId(int _id) {
        user_id = _id;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUsername(String name) {
        username = name;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String pwd) {
        password = pwd;
    }

    public String getPassword() {
        return password;
    }
}
