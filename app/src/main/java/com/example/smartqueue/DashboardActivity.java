package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvAnnouncementMessage;
    private MaterialCardView btnQuickBook, btnMyQueues;
    private RecyclerView recyclerRecent;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        tvAnnouncementMessage = findViewById(R.id.tvAnnouncementMessage);
        btnQuickBook = findViewById(R.id.btnQuickBook);
        btnMyQueues = findViewById(R.id.btnMyQueues);
        recyclerRecent = findViewById(R.id.recyclerRecent);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Load announcement from Firestore (or show default)
        loadAnnouncement();

        // Load dummy recent activity
        setupRecyclerView();

        // Button actions
        btnQuickBook.setOnClickListener(v -> {
            Toast.makeText(this, "Navigating to Quick Book...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, BookActivity.class));
        });

        btnMyQueues.setOnClickListener(v -> {
            Toast.makeText(this, "Opening My Queues...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, QueueStatusActivity.class));
        });

        // Bottom navigation actions
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already here
                return true;
            } else if (id == R.id.nav_book) {
                startActivity(new Intent(this, BookActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadAnnouncement() {
        db.collection("announcements")
                .document("latest")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String message = document.getString("message");
                        tvAnnouncementMessage.setText(message);
                    } else {
                        tvAnnouncementMessage.setText("Currently no new updates.");
                    }
                })
                .addOnFailureListener(e -> tvAnnouncementMessage.setText("Error loading announcements."));
    }

    private void setupRecyclerView() {
        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<Map<String, String>> recentList = new ArrayList<>();

        // Dummy data
        Map<String, String> item1 = new HashMap<>();
        item1.put("title", "Booked: Library Seat");
        item1.put("time", "Today, 10:30 AM");

        Map<String, String> item2 = new HashMap<>();
        item2.put("title", "Joined: Cafeteria Queue");
        item2.put("time", "Yesterday, 1:15 PM");

        recentList.add(item1);
        recentList.add(item2);

        recyclerRecent.setAdapter(new RecentAdapter(recentList));
    }
}
