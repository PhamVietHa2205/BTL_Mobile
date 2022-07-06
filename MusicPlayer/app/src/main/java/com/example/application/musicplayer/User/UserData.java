package com.example.application.musicplayer.User;

public class UserData {
    private String name;
    private String pass;
    private String email;
    private boolean admin;

    public UserData() {
    }

    public UserData(String email, String name, String pass, boolean admin) {
        this.email = email;
        this.name = name;
        this.pass = pass;
        this.admin = admin;
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPass() {
        return pass;
    }

    public boolean isAdmin() {
        return admin;
    }
}
