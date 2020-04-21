package com.example.socialmediaapp.models;

public class ModelChat {
    String message;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDilihat() {
        return dilihat;
    }

    public void setDilihat(boolean dilihat) {
        this.dilihat = dilihat;
    }

    String receiver;

    public ModelChat() {
    }

    String sender;

    public ModelChat(String message, String receiver, String sender, String timestamp, boolean dilihat) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.dilihat = dilihat;
    }

    String timestamp;


    boolean dilihat;
}
