// TimeSlotModel.java
package com.example.smartqueue;

public class TimeSlotModel {
    private String timeRange;
    private String startTime;
    private String endTime;
    private boolean isAvailable;
    private boolean isSelected;

    public TimeSlotModel() {}

    public TimeSlotModel(String timeRange, String startTime, String endTime, boolean isAvailable) {
        this.timeRange = timeRange;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}