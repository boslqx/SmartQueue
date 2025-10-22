package com.example.smartqueue;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LecturerModel {
    private String id;
    private String name;
    private String department;
    private String email;
    private String office_location;
    private int weekly_hours;
    private Map<String, List<String>> consultation_schedule; // day -> list of "HH:mm-HH:mm"
    private Timestamp created_at;
    private Timestamp updated_at;

    // Runtime field (not from Firestore)
    private int booked_hours;

    // Empty constructor for Firestore
    public LecturerModel() {
        this.weekly_hours = 5;
        this.booked_hours = 0;
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

    public String getOffice_location() { return office_location; }
    public void setOffice_location(String office_location) { this.office_location = office_location; }

    public int getWeekly_hours() { return weekly_hours; }
    public void setWeekly_hours(int weekly_hours) { this.weekly_hours = weekly_hours; }

    public Map<String, List<String>> getConsultation_schedule() { return consultation_schedule; }
    public void setConsultation_schedule(Map<String, List<String>> consultation_schedule) {
        this.consultation_schedule = consultation_schedule;
    }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }

    public Timestamp getUpdated_at() { return updated_at; }
    public void setUpdated_at(Timestamp updated_at) { this.updated_at = updated_at; }

    public int getBooked_hours() { return booked_hours; }
    public void setBooked_hours(int booked_hours) { this.booked_hours = booked_hours; }

    // Helper methods
    public List<String> getAvailableDays() {
        if (consultation_schedule == null || consultation_schedule.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(consultation_schedule.keySet());
    }

    public List<String> getSlotsForDay(String dayOfWeek) {
        if (consultation_schedule == null || !consultation_schedule.containsKey(dayOfWeek)) {
            return new ArrayList<>();
        }
        return consultation_schedule.get(dayOfWeek);
    }

    public String getFormattedAvailableDays() {
        if (consultation_schedule == null || consultation_schedule.isEmpty()) {
            return "Not available";
        }

        StringBuilder formatted = new StringBuilder();
        List<String> days = new ArrayList<>(consultation_schedule.keySet());

        for (int i = 0; i < days.size(); i++) {
            String day = days.get(i);
            // Capitalize first 3 letters
            formatted.append(day.substring(0, 1).toUpperCase())
                    .append(day.substring(1, Math.min(3, day.length())));

            if (i < days.size() - 1) {
                formatted.append(", ");
            }
        }
        return formatted.toString();
    }

    public boolean hasAvailableHours() {
        return booked_hours < weekly_hours;
    }

    public int getAvailableHours() {
        return Math.max(0, weekly_hours - booked_hours);
    }

    public String getAvailabilityStatus() {
        if (!hasAvailableHours()) {
            return "Fully Booked";
        }
        return getAvailableHours() + "/" + weekly_hours + " hours available";
    }

    public int getTotalSlotsCount() {
        if (consultation_schedule == null) return 0;

        int count = 0;
        for (List<String> slots : consultation_schedule.values()) {
            count += slots.size();
        }
        return count;
    }
}