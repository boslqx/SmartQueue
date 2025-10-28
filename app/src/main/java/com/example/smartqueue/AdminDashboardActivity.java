package com.example.smartqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {
    private CardView cardAnnouncements, cardLogout;
    private TextView tvAdminWelcome, tvStatsAnnouncements, tvStatsUsers, tvStatsServices;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupClickListeners();
        loadStats();

        Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
    }

    private void initializeViews() {
        // Header
        tvAdminWelcome = findViewById(R.id.tvAdminWelcome);

        // Stats cards
        tvStatsAnnouncements = findViewById(R.id.tvStatsAnnouncements);
        tvStatsUsers = findViewById(R.id.tvStatsUsers);
        tvStatsServices = findViewById(R.id.tvStatsServices);

        // Menu cards
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardLogout = findViewById(R.id.cardLogout);

        // Set admin welcome message
        if (tvAdminWelcome != null) {
            tvAdminWelcome.setText("Admin Dashboard");
        }
    }

    private void setupClickListeners() {
        // Announcements Management
        if (cardAnnouncements != null) {
            cardAnnouncements.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(this, AnnouncementManagementActivity.class));
                } catch (Exception e) {
                    Toast.makeText(this, "Announcement management not available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Logout
        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadStats() {
        // Load announcements count
        db.collection("announcements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (tvStatsAnnouncements != null) {
                        tvStatsAnnouncements.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvStatsAnnouncements != null) {
                        tvStatsAnnouncements.setText("0");
                    }
                });

        // Load users count
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (tvStatsUsers != null) {
                        tvStatsUsers.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvStatsUsers != null) {
                        tvStatsUsers.setText("0");
                    }
                });

        // Load services count
        db.collection("services")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (tvStatsServices != null) {
                        tvStatsServices.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvStatsServices != null) {
                        tvStatsServices.setText("0");
                    }
                });
    }
}