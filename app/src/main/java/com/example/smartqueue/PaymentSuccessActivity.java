package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_success_activity); // Make sure this matches your XML filename

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.available_color));

        TextView tvSuccess = findViewById(R.id.tvSuccess);
        TextView tvBookingId = findViewById(R.id.tvBookingId);
        TextView tvAmount = findViewById(R.id.tvAmount);
        Button btnDone = findViewById(R.id.btnDone);

        // Get data from intent
        String bookingId = getIntent().getStringExtra("bookingId");
        double amount = getIntent().getDoubleExtra("amount", 0.0);

        // Set dynamic content
        tvSuccess.setText(getString(R.string.payment_successful));

        if (bookingId != null) {
            String displayBookingId = bookingId.length() > 8 ? bookingId.substring(0, 8).toUpperCase() : bookingId.toUpperCase();
            tvBookingId.setText(getString(R.string.booking_id_prefix) + " " + displayBookingId);
        } else {
            tvBookingId.setText(getString(R.string.booking_id_prefix) + " " + getString(R.string.booking_id_placeholder));
        }

        tvAmount.setText(getString(R.string.amount_prefix) + " RM " + String.format("%.2f", amount));

        btnDone.setOnClickListener(v -> {
            // Navigate back to main activity or dashboard
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Auto-redirect after 5 seconds
        new Handler().postDelayed(() -> {
            if (!isFinishing()) {
                btnDone.performClick();
            }
        }, 5000);
    }
}