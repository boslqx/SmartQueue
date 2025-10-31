package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView btnBack;

    // Cards
    private CardView cardAnnouncements, cardLecturer, cardClosedSlots;
    private CardView cardBookings, cardUsers, cardStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();

        Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);

        // Menu cards
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardLecturer = findViewById(R.id.cardLecturer);
        cardClosedSlots = findViewById(R.id.cardClosedSlots);
        cardBookings = findViewById(R.id.cardBookings);
        cardUsers = findViewById(R.id.cardUsers);
        cardStatistics = findViewById(R.id.cardStatistics);
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Announcements Management
        if (cardAnnouncements != null) {
            cardAnnouncements.setOnClickListener(v -> {
                startActivity(new Intent(this, AnnouncementManagementActivity.class));
            });
        }
        // Lecturer Management - Fixed redirect
        if (cardLecturer != null) {
            cardLecturer.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminLecturerManagementActivity.class);
                startActivity(intent);
            });
        }

        // Closed Time Slots
        if (cardClosedSlots != null) {
            cardClosedSlots.setOnClickListener(v -> {
                startActivity(new Intent(this, ClosedTimeSlotsActivity.class));
            });
        }

        // View All Bookings
        if (cardBookings != null) {
            cardBookings.setOnClickListener(v -> {
                startActivity(new Intent(this, BookingManagementActivity.class));
            });
        }

        // Users Management
        if (cardUsers != null) {
            cardUsers.setOnClickListener(v -> {
                startActivity(new Intent(this, UserManagementActivity.class));
            });
        }

        // Statistics
        if (cardStatistics != null) {
            cardStatistics.setOnClickListener(v -> {
                Toast.makeText(this, "Statistics - Coming Soon!", Toast.LENGTH_SHORT).show();
                // TODO: Create AdminStatisticsActivity
            });
        }
    }
}