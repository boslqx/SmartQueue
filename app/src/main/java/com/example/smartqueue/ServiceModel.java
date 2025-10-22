package com.example.smartqueue;

public class ServiceModel {
    private String name;
    private String type;
    private int count;
    private String available_from;
    private String available_to;
    private int max_duration;
    private boolean is_paid;
    private String layout_type;
    private double price;

    // Additional fields for specific services
    private int large_rooms;  // For discussion_room
    private int small_rooms;  // For discussion_room
    private int weekly_hours; // For lecturer_consultation

    // Empty constructor required for Firestore
    public ServiceModel() {}

    // Constructor with all fields
    public ServiceModel(String name, String type, int count, String available_from,
                        String available_to, int max_duration, boolean is_paid,
                        String layout_type, double price) {
        this.name = name;
        this.type = type;
        this.count = count;
        this.available_from = available_from;
        this.available_to = available_to;
        this.max_duration = max_duration;
        this.is_paid = is_paid;
        this.layout_type = layout_type;
        this.price = price;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getAvailable_from() { return available_from; }
    public void setAvailable_from(String available_from) { this.available_from = available_from; }

    public String getAvailable_to() { return available_to; }
    public void setAvailable_to(String available_to) { this.available_to = available_to; }

    public int getMax_duration() { return max_duration; }
    public void setMax_duration(int max_duration) { this.max_duration = max_duration; }

    public boolean isIs_paid() { return is_paid; }
    public void setIs_paid(boolean is_paid) { this.is_paid = is_paid; }

    public String getLayout_type() { return layout_type; }
    public void setLayout_type(String layout_type) { this.layout_type = layout_type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getLarge_rooms() { return large_rooms; }
    public void setLarge_rooms(int large_rooms) { this.large_rooms = large_rooms; }

    public int getSmall_rooms() { return small_rooms; }
    public void setSmall_rooms(int small_rooms) { this.small_rooms = small_rooms; }

    public int getWeekly_hours() { return weekly_hours; }
    public void setWeekly_hours(int weekly_hours) { this.weekly_hours = weekly_hours; }
}