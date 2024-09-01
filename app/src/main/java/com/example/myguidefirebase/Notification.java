package com.example.myguidefirebase;

import java.util.Date;

public class Notification {

    private String title;
    private String message;
    private boolean isRead;
    private Date timestamp; // Add this field

    public Notification() {
        // Default constructor required for calls to DataSnapshot.getValue(Notification.class)
    }

    public Notification(String title, String message, boolean isRead, Date timestamp) {
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
