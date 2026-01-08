package com.example.foundit;

import java.io.Serializable;
import java.util.HashMap;

public class ReportItem implements Serializable {

    private String id;
    private String itemName;
    private String category;
    private String location;
    private String description;
    private String contact;
    private String imageBase64;
    private String ownerId;
    private String date;
    private double latitude;
    private double longitude;
    private String status;
    private HashMap<String, Boolean> likes;

    public ReportItem() {
        this.likes = new HashMap<>();
    }

    public ReportItem(String id, String itemName, String category, String location,
                      String description, String contact, String imageBase64,
                      String ownerId, String date) {
        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.location = location;
        this.description = description;
        this.contact = contact;
        this.imageBase64 = imageBase64;
        this.ownerId = ownerId;
        this.date = date;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.status = "open";
        this.likes = new HashMap<>();
    }

    // ===== GETTERS =====
    public String getId() { return id; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getContact() { return contact; }
    public String getImageBase64() { return imageBase64; }
    public String getOwnerId() { return ownerId; }
    public String getDate() { return date; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getStatus() { return status; }
    public HashMap<String, Boolean> getLikes() { return likes; }

    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }

    // ===== SETTERS =====
    public void setId(String id) { this.id = id; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setCategory(String category) { this.category = category; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setContact(String contact) { this.contact = contact; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setDate(String date) { this.date = date; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setStatus(String status) { this.status = status; }
    public void setLikes(HashMap<String, Boolean> likes) { this.likes = likes; }
}
