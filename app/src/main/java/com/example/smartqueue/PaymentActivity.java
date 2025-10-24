package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView tvService, tvAmount, tvLocation, tvTimeSlot;
    private RadioGroup rgPaymentMethod;
    private Button btnPay;
    private ImageView btnBack;

    private String serviceType, serviceName, locationId, locationName, startTime, endTime;
    private double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);

        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews();
        getIntentData();
        displayPaymentInfo();
        setupBackButton();

        btnPay.setOnClickListener(v -> processPayment());

        // Set up radio button change listener for better UX
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            updatePayButtonState();
        });
    }

    private void initializeViews() {
        tvService = findViewById(R.id.tvService);
        tvAmount = findViewById(R.id.tvAmount);
        tvLocation = findViewById(R.id.tvLocation);
        tvTimeSlot = findViewById(R.id.tvTimeSlot);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        btnPay = findViewById(R.id.btnPay);
        btnBack = findViewById(R.id.btnBack);

        // Initially disable pay button until method selected
        btnPay.setEnabled(false);
        btnPay.setAlpha(0.6f);
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        if (!btnPay.isEnabled() || btnPay.getText().toString().equals("Processing...")) {
            showBackConfirmationDialog();
        } else {
            navigateBack();
        }
    }

    private void showBackConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Leave Payment?")
                .setMessage("Are you sure you want to leave? Your payment progress will be lost.")
                .setPositiveButton("Leave", (dialog, which) -> navigateBack())
                .setNegativeButton("Stay", null)
                .show();
    }

    private void navigateBack() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void getIntentData() {
        serviceType = getIntent().getStringExtra("serviceType");
        serviceName = getIntent().getStringExtra("serviceName");
        locationId = getIntent().getStringExtra("locationId");
        locationName = getIntent().getStringExtra("locationName");
        startTime = getIntent().getStringExtra("startTime");
        endTime = getIntent().getStringExtra("endTime");
        price = getIntent().getDoubleExtra("price", 0.0);
    }

    private void displayPaymentInfo() {
        tvService.setText(serviceName);
        tvAmount.setText("RM " + String.format("%.2f", price));

        if (locationName != null) {
            tvLocation.setText(locationName);
        }

        if (startTime != null && endTime != null) {
            tvTimeSlot.setText(startTime + " - " + endTime);
        }
    }

    private void updatePayButtonState() {
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        boolean hasSelection = selectedId != -1;

        btnPay.setEnabled(hasSelection);
        btnPay.setAlpha(hasSelection ? 1.0f : 0.6f);
    }

    private void processPayment() {
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        String method = selectedId == R.id.rbTng ? "Touch 'n Go" : "Credit/Debit Card";

        // Show processing state
        btnPay.setEnabled(false);
        btnPay.setText("Processing...");

        // Simulate payment process with delay
        simulatePaymentProcessing(method);
    }

    private void simulatePaymentProcessing(String paymentMethod) {
        // Simulate API call delay
        new Handler().postDelayed(() -> {
            // Simulate payment success (in real app, handle success/failure from gateway)
            boolean paymentSuccess = true; // Always success for simulation

            if (paymentSuccess) {
                savePaymentAndBooking(paymentMethod);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(PaymentActivity.this, "Payment failed. Please try again.", Toast.LENGTH_LONG).show();
                    btnPay.setEnabled(true);
                    btnPay.setText(R.string.pay_now);
                });
            }
        }, 2000); // 2 second delay
    }

    private void savePaymentAndBooking(String paymentMethod) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "guest@example.com";
        String userName = auth.getCurrentUser() != null ? auth.getCurrentUser().getDisplayName() : "Guest";
        if (userName == null) userName = "User";

        String paymentId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();

        // Generate date string from startTime if needed
        String dateString = generateDateString();

        // ===== FIXED: All fields now use snake_case consistently =====

        // First, save payment record
        Map<String, Object> payment = new HashMap<>();
        payment.put("payment_id", paymentId);
        payment.put("user_id", userId);
        payment.put("amount", price);
        payment.put("payment_method", paymentMethod);
        payment.put("status", "completed");
        payment.put("timestamp", Timestamp.now());
        payment.put("service_type", serviceType);

        // Then save booking with payment reference - ALL snake_case
        Map<String, Object> booking = new HashMap<>();
        booking.put("booking_id", bookingId);
        booking.put("user_id", userId);
        booking.put("user_email", userEmail);
        booking.put("user_name", userName);
        booking.put("payment_id", paymentId);
        booking.put("service_type", serviceType);
        booking.put("service_name", serviceName);
        booking.put("location_id", locationId);
        booking.put("location_name", locationName);
        booking.put("start_time", startTime);  // Changed from startTime to start_time
        booking.put("end_time", endTime);      // Changed from endTime to end_time
        booking.put("date", dateString);       // Added date field
        booking.put("duration", 1);            // Added duration field (calculate if needed)
        booking.put("timestamp", Timestamp.now());
        booking.put("created_at", Timestamp.now());
        booking.put("updated_at", Timestamp.now());
        booking.put("status", "confirmed");
        booking.put("amount", price);
        booking.put("payment_status", "paid");

        // Save payment first, then booking
        db.collection("payments").document(paymentId).set(payment)
                .addOnSuccessListener(aVoid -> {
                    // Payment saved successfully, now save booking
                    db.collection("bookings").document(bookingId).set(booking)
                            .addOnSuccessListener(aVoid1 -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(PaymentActivity.this, "Payment Successful!", Toast.LENGTH_LONG).show();
                                    Intent success = new Intent(PaymentActivity.this, PaymentSuccessActivity.class);
                                    success.putExtra("bookingId", bookingId);
                                    success.putExtra("amount", price);
                                    startActivity(success);
                                    finish();
                                });
                            })
                            .addOnFailureListener(e -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(PaymentActivity.this, "Error saving booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    btnPay.setEnabled(true);
                                    btnPay.setText(R.string.pay_now);
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(PaymentActivity.this, "Error processing payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnPay.setEnabled(true);
                        btnPay.setText(R.string.pay_now);
                    });
                });
    }

    /**
     * Generate date string in yyyy-MM-dd format
     * This should ideally come from the booking selection screen
     */
    private String generateDateString() {
        // If you pass date from previous activity, use that
        String dateFromIntent = getIntent().getStringExtra("date");
        if (dateFromIntent != null && !dateFromIntent.isEmpty()) {
            return dateFromIntent;
        }

        // Otherwise use current date as fallback
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }
}