package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookActivity extends AppCompatActivity {

    private CardView btnDiscussionRoom, btnPoolTable, btnPingPong, btnCourt, btnMusicRoom, btnConsultation;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views
        btnDiscussionRoom = findViewById(R.id.btnDiscussionRoom);
        btnPoolTable = findViewById(R.id.btnPoolTable);
        btnPingPong = findViewById(R.id.btnPingPong);
        btnCourt = findViewById(R.id.btnCourt);
        btnMusicRoom = findViewById(R.id.btnMusicRoom);
        btnConsultation = findViewById(R.id.btnConsultation);

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Click events
        btnDiscussionRoom.setOnClickListener(v -> navigateToSlots("discussion_room"));
        btnPoolTable.setOnClickListener(v -> navigateToSlots("pool_table"));
        btnPingPong.setOnClickListener(v -> navigateToSlots("ping_pong"));
        btnCourt.setOnClickListener(v -> navigateToSlots("court"));
        btnMusicRoom.setOnClickListener(v -> navigateToSlots("music_room"));
        btnConsultation.setOnClickListener(v -> navigateToSlots("consultation"));

        // Bottom navigation actions
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_book) {
                // Already here
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // Set the current item as selected
        bottomNav.setSelectedItemId(R.id.nav_book);
    }

    private void navigateToSlots(String serviceType) {
        Toast.makeText(this, "Selected: " + serviceType.replace("_", " "), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(BookActivity.this, SelectSlotActivity.class);
        intent.putExtra("serviceType", serviceType);
        startActivity(intent);
    }
}