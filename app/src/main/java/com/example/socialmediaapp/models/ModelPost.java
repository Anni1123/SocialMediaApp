package com.example.socialmediaapp.models;

public class ModelPost {
    public ModelPost() {
    }

    String description;
    String ptime;

    public ModelPost(String description, String ptime, String title, String udp, String uemail, String uid, String uimage, String uname) {
        this.description = description;
        this.ptime = ptime;
        this.title = title;
        this.udp = udp;
        this.uemail = uemail;
        this.uid = uid;
        this.uimage = uimage;
        this.uname = uname;
    }

    String title;
    String udp;
    String uemail;
    String uid;
    String uimage;
    String uname;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPtime() {
        return ptime;
    }

    public void setPtime(String ptime) {
        this.ptime = ptime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUdp() {
        return udp;
    }

    public void setUdp(String udp) {
        this.udp = udp;
    }

    public String getUemail() {
        return uemail;
    }

    public void setUemail(String uemail) {
        this.uemail = uemail;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUimage() {
        return uimage;
    }

    public void setUimage(String uimage) {
        this.uimage = uimage;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }
}
