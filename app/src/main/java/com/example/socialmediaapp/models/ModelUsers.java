package com.example.socialmediaapp.models;

public class ModelUsers {
    String name;

    public String getName() {
        return name;
    }

    public ModelUsers() {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    String email;

    public ModelUsers(String name, String email, String image, String cover, String phone, String uid) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.cover = cover;
        this.phone = phone;
        this.uid = uid;
    }

    String image;
    String cover;
    String phone;
    String uid;
}
