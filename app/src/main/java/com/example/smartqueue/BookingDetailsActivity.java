package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookingDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ImageView btnBack;
    private TextView tvBookingId, tvServiceName, tvLocation, tvDate, tvTimeSlot;
    private TextView tvDuration, tvStatus, tvAmount, tvPaymentMethod;
    private TextView tvCreatedAt, tvUpdatedAt;
    private Button btnCancelBooking, btnBookAgain;

    private String bookingId;
    private BookingModel currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_details_activity);

        db = FirebaseFirestore.getInstance();

        initializeViews();

        bookingId = getIntent().getStringExtra("bookingId");
        if (bookingId == null) {
            Toast.makeText(this, "Error: No booking ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBookingDetails();

        btnBack.setOnClickListener(v -> finish());
        btnCancelBooking.setOnClickListener(v -> showCancelConfirmation());
        btnBookAgain.setOnClickListener(v -> bookAgain());
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvLocation = findViewById(R.id.tvLocation);
        tvDate = findViewById(R.id.tvDate);
        tvTimeSlot = findViewById(R.id.tvTimeSlot);
        tvDuration = findViewById(R.id.tvDuration);
        tvStatus = findViewById(R.id.tvStatus);
        tvAmount = findViewById(R.id.tvAmount);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvUpdatedAt = findViewById(R.id.tvUpdatedAt);
        btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnBookAgain = findViewById(R.id.btnBookAgain);
    }

    private void loadBookingDetails() {
        db.collection("bookings").document(bookingId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        currentBooking = new BookingModel();
                        currentBooking.setDocumentId(document.getId());
                        currentBooking.setUserId(document.getString("user_id"));
                        currentBooking.setUserEmail(document.getString("user_email"));
                        currentBooking.setUserName(document.getString("user_name"));
                        currentBooking.setServiceType(document.getString("service_type"));
                        currentBooking.setServiceName(document.getString("service_name"));
                        currentBooking.setLocationId(document.getString("location_id"));
                        currentBooking.setDate(document.getString("date"));
                        currentBooking.setStartTime(document.getString("start_time"));
                        currentBooking.setEndTime(document.getString("end_time"));
                        currentBooking.setDuration(document.getLong("duration") != null ?
                                document.getLong("duration").intValue() : 1);
                        currentBooking.setAmount(document.getDouble("amount") != null ?
                                document.getDouble("amount") : 0.0);
                        currentBooking.setPaymentStatus(document.getString("payment_status"));
                        currentBooking.setStatus(document.getString("status"));
                        currentBooking.setCreatedAt(document.getTimestamp("created_at"));
                        currentBooking.setUpdatedAt(document.getTimestamp("updated_at"));

                        displayBookingDetails();
                    } else {
                        Toast.makeText(this, "Booking not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading booking: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayBookingDetails() {
        tvBookingId.setText("Booking ID: " + bookingId);
        tvServiceName.setText(currentBooking.getServiceName());
        tvLocation.setText(currentBooking.getLocationId());
        tvDate.setText(currentBooking.getDate());
        tvTimeSlot.setText(currentBooking.getTimeSlot());
        tvDuration.setText(currentBooking.getDuration() + " hour" +
                (currentBooking.getDuration() > 1 ? "s" : ""));

        // Status with color (using computed status)
        String displayStatus = currentBooking.getComputedStatus();
        tvStatus.setText(displayStatus.toUpperCase());
        tvStatus.setTextColor(currentBooking.getStatusColor(this));

        // Amount
        if (currentBooking.isPaid()) {
            tvAmount.setText(String.format("RM %.2f", currentBooking.getAmount()));
            tvPaymentMethod.setText(currentBooking.getPaymentStatus());
        } else {
            tvAmount.setText("Free");
            tvPaymentMethod.setText("N/A");
        }

        // Timestamps
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        if (currentBooking.getCreatedAt() != null) {
            tvCreatedAt.setText(sdf.format(currentBooking.getCreatedAt().toDate()));
        }
        if (currentBooking.getUpdatedAt() != null) {
            tvUpdatedAt.setText(sdf.format(currentBooking.getUpdatedAt().toDate()));
        }

        // Show/hide cancel button
        if (currentBooking.canCancel()) {
            btnCancelBooking.setVisibility(View.VISIBLE);
        } else {
            btnCancelBooking.setVisibility(View.GONE);
        }

        // Show/hide book again button
        if (currentBooking.canRebook()) {
            btnBookAgain.setVisibility(View.VISIBLE);
        } else {
            btnBookAgain.setVisibility(View.GONE);
        }
    }

    private void showCancelConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelBooking())
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelBooking() {
        btnCancelBooking.setEnabled(false);
        btnCancelBooking.setText("Cancelling...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "cancelled");
        updates.put("updated_at", Timestamp.now());

        db.collection("bookings").document(bookingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Log cancellation
                    logCancellation();

                    Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to cancel booking: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnCancelBooking.setEnabled(true);
                    btnCancelBooking.setText("Cancel Booking");
                });
    }

    private void logCancellation() {
        Map<String, Object> cancelLog = new HashMap<>();
        cancelLog.put("booking_id", bookingId);
        cancelLog.put("user_id", currentBooking.getUserId());
        cancelLog.put("service_name", currentBooking.getServiceName());
        cancelLog.put("cancelled_at", Timestamp.now());
        cancelLog.put("original_date", currentBooking.getDate());
        cancelLog.put("original_time", currentBooking.getTimeSlot());

        db.collection("cancel_log").add(cancelLog);
    }

    private void bookAgain() {
        if (currentBooking == null) return;

        String serviceType = currentBooking.getServiceType();

        // For lecturer consultation, go to lecturer list first
        if ("lecturer_consultation".equalsIgnoreCase(serviceType)) {
            Intent intent = new Intent(this, SelectSlotActivity.class);
            intent.putExtra("serviceType", serviceType);
            startActivity(intent);
            finish();
            return;
        }

        // Load service info first
        db.collection("services").document(serviceType)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        ServiceModel serviceModel = document.toObject(ServiceModel.class);
                        if (serviceModel != null) {
                            // Navigate to TimeSlotActivity with booking data
                            Intent intent = new Intent(this, TimeSlotActivity.class);
                            intent.putExtra("serviceType", currentBooking.getServiceType());
                            intent.putExtra("serviceName", currentBooking.getServiceName());
                            intent.putExtra("locationId", currentBooking.getLocationId());
                            intent.putExtra("availableFrom", serviceModel.getAvailable_from());
                            intent.putExtra("availableTo", serviceModel.getAvailable_to());
                            intent.putExtra("maxDuration", serviceModel.getMax_duration());
                            intent.putExtra("isPaid", serviceModel.isIs_paid());
                            intent.putExtra("price", serviceModel.getPrice());

                            // Optional: pre-select the same time if available
                            intent.putExtra("suggestedStartTime", currentBooking.getStartTime());
                            intent.putExtra("suggestedEndTime", currentBooking.getEndTime());

                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading service: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}