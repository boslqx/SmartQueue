package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfirmBookingActivity extends AppCompatActivity {

    private static final String TAG = "ConfirmBookingActivity";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String serviceType, serviceName, locationId, date, startTime, endTime, extraInfo;
    private boolean isPaid;
    private double price;

    private TextView tvServiceName, tvLocation, tvDate, tvTimeSlot, tvDuration, tvTotalAmount;
    private Button btnConfirm, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_booking);

        initializeFirebase();
        initializeViews();
        getIntentData();
        setupUI();
        setupClickListeners();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        tvServiceName = findViewById(R.id.tvServiceName);
        tvLocation = findViewById(R.id.tvLocation);
        tvDate = findViewById(R.id.tvDate);
        tvTimeSlot = findViewById(R.id.tvTimeSlot);
        tvDuration = findViewById(R.id.tvDuration);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        serviceType = intent.getStringExtra("serviceType");
        serviceName = intent.getStringExtra("serviceName");
        locationId = intent.getStringExtra("locationId");
        extraInfo = intent.getStringExtra("extraInfo");
        date = intent.getStringExtra("date");
        startTime = intent.getStringExtra("startTime");
        endTime = intent.getStringExtra("endTime");
        isPaid = intent.getBooleanExtra("isPaid", false);
        price = intent.getDoubleExtra("price", 0.0);

        Log.d(TAG, "Booking details - Service: " + serviceType + ", Location: " + locationId);
        Log.d(TAG, "Date: " + date + ", Time: " + startTime + " - " + endTime);
        Log.d(TAG, "Paid: " + isPaid + ", Price: " + price);
    }

    private void setupUI() {
        // Set service details
        tvServiceName.setText(serviceName);
        tvLocation.setText(locationId + (extraInfo != null ? " (" + extraInfo + ")" : ""));

        // Format and set date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
            String formattedDate = outputFormat.format(inputFormat.parse(date));
            tvDate.setText(formattedDate);
        } catch (Exception e) {
            tvDate.setText(date);
        }

        // Set time slot
        tvTimeSlot.setText(startTime + " - " + endTime);

        // Calculate and set duration
        int duration = calculateDuration(startTime, endTime);
        tvDuration.setText(duration + " hour" + (duration > 1 ? "s" : ""));

        // Set total amount
        if (isPaid) {
            double totalAmount = duration * price;
            tvTotalAmount.setText(String.format(Locale.getDefault(), "RM %.2f", totalAmount));
        } else {
            tvTotalAmount.setText("Free");
        }
    }

    private int calculateDuration(String start, String end) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            long startMillis = format.parse(start).getTime();
            long endMillis = format.parse(end).getTime();
            return (int) ((endMillis - startMillis) / (1000 * 60 * 60));
        } catch (Exception e) {
            Log.e(TAG, "Error calculating duration", e);
            return 1; // Default to 1 hour if calculation fails
        }
    }

    private void setupClickListeners() {
        btnConfirm.setOnClickListener(v -> confirmBooking());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void confirmBooking() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Disable button to prevent double-clicking
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Processing...");

        if (isPaid) {
            // Redirect to PaymentActivity
            navigateToPayment();
        } else {
            // Directly create booking for free services
            createBookingInFirestore();
        }
    }

    private void navigateToPayment() {
        Intent intent = new Intent(ConfirmBookingActivity.this, PaymentActivity.class);
        intent.putExtra("serviceType", serviceType);
        intent.putExtra("serviceName", serviceName);
        intent.putExtra("locationId", locationId);
        intent.putExtra("extraInfo", extraInfo);
        intent.putExtra("date", date);
        intent.putExtra("startTime", startTime);
        intent.putExtra("endTime", endTime);
        intent.putExtra("price", price);
        intent.putExtra("duration", calculateDuration(startTime, endTime));
        startActivity(intent);
        finish();
    }

    private void createBookingInFirestore() {
        String userId = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();
        String userName = mAuth.getCurrentUser().getDisplayName();

        if (userName == null || userName.isEmpty()) {
            userName = userEmail != null ? userEmail.split("@")[0] : "User";
        }

        int duration = calculateDuration(startTime, endTime);
        double amount = isPaid ? duration * price : 0.0;

        // Match your database schema exactly
        Map<String, Object> booking = new HashMap<>();
        booking.put("service_type", serviceType);  // Changed to match schema
        booking.put("service_name", serviceName);  // Changed to match schema
        booking.put("location_id", locationId);    // Changed to match schema
        booking.put("user_id", userId);            // Changed to match schema
        booking.put("user_name", userName);        // Added
        booking.put("user_email", userEmail);      // Added
        booking.put("date", date);
        booking.put("start_time", startTime);      // Changed to match schema
        booking.put("end_time", endTime);          // Changed to match schema
        booking.put("duration", duration);
        booking.put("status", "confirmed");
        booking.put("payment_status", isPaid ? "pending" : "free");  // Added
        booking.put("amount", amount);
        booking.put("created_at", Timestamp.now());
        booking.put("updated_at", Timestamp.now());

        Log.d(TAG, "Creating booking with data: " + booking.toString());

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Booking created with ID: " + documentReference.getId());
                    showSuccessAndNavigate(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating booking: " + e.getMessage(), e);
                    Toast.makeText(this, "Booking failed: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // Re-enable button
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Confirm Booking");
                });
    }

    private void showSuccessAndNavigate(String bookingId) {
        Toast.makeText(this, "Booking confirmed successfully!", Toast.LENGTH_LONG).show();

        // Navigate to booking status or back to dashboard
        Intent intent = new Intent(ConfirmBookingActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}