package com.example.smartqueue;

import com.google.firebase.Timestamp;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String school;
    private boolean isAdmin;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Empty constructor required for Firestore
    public UserModel() {
        this.isAdmin = false;
    }

    // Constructor with all fields
    public UserModel(String uid, String name, String email, String school, boolean isAdmin) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.school = school;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
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

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public String getDisplayName() {
        return name != null && !name.isEmpty() ? name : "User";
    }

    public String getRoleDisplay() {
        return isAdmin ? "Admin" : "Regular User";
    }

    public String getInitials() {
        if (name == null || name.isEmpty()) {
            return email != null && !email.isEmpty() ? email.substring(0, 1).toUpperCase() : "?";
        }

        String[] nameParts = name.trim().split("\\s+");
        if (nameParts.length >= 2) {
            return (nameParts[0].substring(0, 1) + nameParts[1].substring(0, 1)).toUpperCase();
        } else {
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", school='" + school + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}