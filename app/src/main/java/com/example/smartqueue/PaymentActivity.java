package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

    private String serviceType, serviceName, locationId, locationName, date, startTime, endTime;
    private int duration;
    private double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews();
        getIntentData();
        displayPaymentInfo();
        setupBackButton();

        btnPay.setOnClickListener(v -> processPayment());

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
        date = getIntent().getStringExtra("date");
        startTime = getIntent().getStringExtra("startTime");
        endTime = getIntent().getStringExtra("endTime");
        duration = getIntent().getIntExtra("duration", 1);
        price = getIntent().getDoubleExtra("price", 0.0);
    }

    private void displayPaymentInfo() {
        tvService.setText(serviceName);
        tvAmount.setText("RM " + String.format("%.2f", price));

        if (locationName != null) {
            tvLocation.setText(locationName);
        } else if (locationId != null) {
            tvLocation.setText(locationId);
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

        btnPay.setEnabled(false);
        btnPay.setText("Processing...");

        simulatePaymentProcessing(method);
    }

    private void simulatePaymentProcessing(String paymentMethod) {
        new Handler().postDelayed(() -> {
            boolean paymentSuccess = true;

            if (paymentSuccess) {
                savePaymentAndBooking(paymentMethod);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(PaymentActivity.this, "Payment failed. Please try again.", Toast.LENGTH_LONG).show();
                    btnPay.setEnabled(true);
                    btnPay.setText(R.string.pay_now);
                });
            }
        }, 2000);
    }

    private void savePaymentAndBooking(String paymentMethod) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "guest@example.com";
        String userName = auth.getCurrentUser() != null ? auth.getCurrentUser().getDisplayName() : "Guest";
        if (userName == null) userName = "User";

        String paymentId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();

        // Ensure we have a date
        String dateString = (date != null && !date.isEmpty()) ? date : generateDateString();

        // *** Create final variables for lambda ***
        final String finalUserEmail = userEmail;
        final String finalUserName = userName;
        final String finalServiceName = serviceName;
        final String finalLocationId = locationId;
        final String finalDate = dateString;
        final String finalStartTime = startTime;
        final String finalEndTime = endTime;
        final int finalDuration = duration;
        final double finalPrice = price;

        // Save payment record
        Map<String, Object> payment = new HashMap<>();
        payment.put("payment_id", paymentId);
        payment.put("user_id", userId);
        payment.put("amount", price);
        payment.put("payment_method", paymentMethod);
        payment.put("status", "completed");
        payment.put("timestamp", Timestamp.now());
        payment.put("service_type", serviceType);

        // Save booking with ALL snake_case fields
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
        booking.put("start_time", startTime);
        booking.put("end_time", endTime);
        booking.put("date", dateString);
        booking.put("duration", duration);
        booking.put("timestamp", Timestamp.now());
        booking.put("created_at", Timestamp.now());
        booking.put("updated_at", Timestamp.now());
        booking.put("status", "confirmed");
        booking.put("amount", price);
        booking.put("payment_status", "paid");

        db.collection("payments").document(paymentId).set(payment)
                .addOnSuccessListener(aVoid -> {
                    db.collection("bookings").document(bookingId).set(booking)
                            .addOnSuccessListener(aVoid1 -> {
                                runOnUiThread(() -> {
                                    // *** Send confirmation email ***
                                    BookingModel bookingModel = new BookingModel();
                                    bookingModel.setUserEmail(finalUserEmail);
                                    bookingModel.setUserName(finalUserName);
                                    bookingModel.setServiceName(finalServiceName);
                                    bookingModel.setLocationId(finalLocationId);
                                    bookingModel.setDate(finalDate);
                                    bookingModel.setStartTime(finalStartTime);
                                    bookingModel.setEndTime(finalEndTime);
                                    bookingModel.setDuration(finalDuration);
                                    bookingModel.setAmount(finalPrice);
                                    bookingModel.setPaymentStatus("paid");
                                    bookingModel.setStatus("confirmed");

                                    // Send email in background
                                    EmailSender.sendBookingConfirmation(bookingModel, bookingId);
                                    // *** END EMAIL CODE ***

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

    private String generateDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }
}