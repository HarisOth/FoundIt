package com.example.foundit;

public class NotificationItem {

    private String id; // Firebase key
    private String message;
    private long timestamp;
    private boolean read;

    public NotificationItem() {}

    public NotificationItem(String message, long timestamp, boolean read) {
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
