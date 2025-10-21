package com.example.smartqueue;

public class DateItemModel {
    private String date; // Format: 2025-10-21
    private String dayOfWeek; // MON, TUE, etc.
    private String dayOfMonth; // 21, 22, etc.
    private String monthYear; // Oct 2025
    private boolean isSelected;
    private boolean isToday;

    public DateItemModel(String date, String dayOfWeek, String dayOfMonth, String monthYear, boolean isToday) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.dayOfMonth = dayOfMonth;
        this.monthYear = monthYear;
        this.isSelected = false;
        this.isToday = isToday;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }
}