package com.example.smartqueue;

public class Announcement {
    private String id;
    private boolean active;
    private long created_at;
    private String message;
    private int priority;
    private String title;
    private String type;

    // Required empty constructor for Firestore
    public Announcement() {}

    public Announcement(String title, String message, String type, int priority) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.active = true;
        this.created_at = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getCreated_at() { return created_at; }
    public void setCreated_at(long created_at) { this.created_at = created_at; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}