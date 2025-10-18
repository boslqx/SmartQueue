package com.example.smartqueue;

import java.util.List;

public class LecturerModel {
    private String id;
    private String name;
    private String department;
    private String email;
    private List<String> available_days;
    private String available_from;
    private String available_to;
    private int weekly_hours;
    private int booked_hours;

    // Empty constructor for Firestore
    public LecturerModel() {}

    public LecturerModel(String id, String name, String department, String email,
                         List<String> available_days, String available_from,
                         String available_to, int weekly_hours, int booked_hours) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.email = email;
        this.available_days = available_days;
        this.available_from = available_from;
        this.available_to = available_to;
        this.weekly_hours = weekly_hours;
        this.booked_hours = booked_hours;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getAvailable_days() { return available_days; }
    public void setAvailable_days(List<String> available_days) { this.available_days = available_days; }

    public String getAvailable_from() { return available_from; }
    public void setAvailable_from(String available_from) { this.available_from = available_from; }

    public String getAvailable_to() { return available_to; }
    public void setAvailable_to(String available_to) { this.available_to = available_to; }

    public int getWeekly_hours() { return weekly_hours; }
    public void setWeekly_hours(int weekly_hours) { this.weekly_hours = weekly_hours; }

    public int getBooked_hours() { return booked_hours; }
    public void setBooked_hours(int booked_hours) { this.booked_hours = booked_hours; }

    // Helper method to format available days
    public String getFormattedAvailableDays() {
        if (available_days == null || available_days.isEmpty()) {
            return "Not available";
        }
        return String.join(", ", available_days).toUpperCase();
    }

    // Check if lecturer has available hours
    public boolean hasAvailableHours() {
        return booked_hours < weekly_hours;
    }
}