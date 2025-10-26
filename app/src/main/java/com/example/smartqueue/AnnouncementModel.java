package com.example.smartqueue;

import com.google.firebase.Timestamp;

public class AnnouncementModel {
    private String id;
    private String title;
    private String message;
    private String type; // "info", "warning", "success", "event"
    private int priority; // Higher number = higher priority
    private boolean active;
    private Timestamp created_at;
    private Timestamp expires_at;

    public AnnouncementModel() {
        this.active = true;
        this.priority = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }

    public Timestamp getExpires_at() { return expires_at; }
    public void setExpires_at(Timestamp expires_at) { this.expires_at = expires_at; }

    // Helper method to get background color based on type
    public int getBackgroundColor() {
        if (type == null) return 0xFFBADFDB; // Default primary color

        switch (type.toLowerCase()) {
            case "warning":
                return 0xFFFFE0B2; // Light orange/amber
            case "success":
                return 0xFFC8E6C9; // Light green
            case "event":
                return 0xFFBBDEFB; // Light blue
            case "info":
            default:
                return 0xFFB3E5FC; // Light cyan
        }
    }

    // Fixed emoji rendering
    public String getIcon() {
        if (type == null) return "ℹ️";

        switch (type.toLowerCase()) {
            case "warning":
                return "⚠️";
            case "success":
                return "✅";
            case "event":
                return "📅";
            case "info":
            default:
                return "ℹ️";
        }
    }
}