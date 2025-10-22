package com.example.smartqueue;

import com.google.firebase.Timestamp;

public class BookingModel {
    private String documentId;
    private String userId;
    private String userEmail;
    private String userName;
    private String serviceType;
    private String serviceName;
    private String locationId;
    private String date;
    private String startTime; // Changed from start_time to match Java naming
    private String endTime;   // Changed from end_time to match Java naming
    private int duration;
    private double amount;
    private String paymentStatus;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Empty constructor required for Firestore
    public BookingModel() {
    }

    public BookingModel(String documentId, String userId, String userEmail, String userName,
                        String serviceType, String serviceName, String locationId,
                        String date, String startTime, String endTime, int duration,
                        double amount, String paymentStatus, String status,
                        Timestamp createdAt, Timestamp updatedAt) {
        this.documentId = documentId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.locationId = locationId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
    public boolean isPaid() {
        return "paid".equalsIgnoreCase(paymentStatus) && amount > 0;
    }

    public boolean isFree() {
        return "free".equalsIgnoreCase(paymentStatus) || amount == 0;
    }

    public boolean canCancel() {
        return "confirmed".equalsIgnoreCase(status);
    }

    public String getTimeSlot() {
        return startTime + " - " + endTime;
    }

    public String getFormattedAmount() {
        if (isFree()) {
            return "Free";
        } else {
            return String.format("RM %.2f", amount);
        }
    }

    public String getStatusDisplay() {
        if (status == null) return "Unknown";
        switch (status.toLowerCase()) {
            case "confirmed":
                return "Confirmed";
            case "completed":
                return "Completed";
            case "cancelled":
                return "Cancelled";
            default:
                return status;
        }
    }
}