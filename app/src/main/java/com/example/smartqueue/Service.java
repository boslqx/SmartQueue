package com.example.smartqueue;

public class Service {
    private String id;
    private String name;
    private String description;
    private String location;
    private long createdAt;

    // Required empty constructor for Firestore
    public Service() {}

    // Constructor with parameters
    public Service(String name, String description, String location) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}