package com.example.smartqueue;

import com.google.firebase.Timestamp;

public class ClosedSlotModel {
    private String id;
    private String date;
    private String start_time;
    private String end_time;
    private String service_type;
    private String reason;
    private Timestamp created_at;

    public ClosedSlotModel() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStart_time() { return start_time; }
    public void setStart_time(String start_time) { this.start_time = start_time; }

    public String getEnd_time() { return end_time; }
    public void setEnd_time(String end_time) { this.end_time = end_time; }

    public String getService_type() { return service_type; }
    public void setService_type(String service_type) { this.service_type = service_type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }

    public String getTimeSlot() {
        return start_time + " - " + end_time;
    }
}