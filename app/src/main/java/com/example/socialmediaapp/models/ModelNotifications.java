package com.example.socialmediaapp.models;

public class ModelNotifications {
    String pid;

    public ModelNotifications() {
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPuid() {
        return puid;
    }

    public void setPuid(String puid) {
        this.puid = puid;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getSuid() {
        return suid;
    }

    public void setSuid(String suid) {
        this.suid = suid;
    }

    public String getSemail() {
        return semail;
    }

    public void setSemail(String semail) {
        this.semail = semail;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getSimage() {
        return simage;
    }

    public void setSimage(String simage) {
        this.simage = simage;
    }

    String timestamp;

    public ModelNotifications(String pid, String timestamp, String puid, String notification, String suid, String semail, String sname, String simage) {
        this.pid = pid;
        this.timestamp = timestamp;
        this.puid = puid;
        this.notification = notification;
        this.suid = suid;
        this.semail = semail;
        this.sname = sname;
        this.simage = simage;
    }

    String puid;
    String notification;
    String suid;
    String semail;
    String sname;
    String simage;

}
