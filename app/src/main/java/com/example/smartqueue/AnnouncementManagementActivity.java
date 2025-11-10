package com.example.smartqueue;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ImageView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementManagementActivity extends AppCompatActivity {

    private static final String TAG = "AnnouncementMgmt";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText etAnnouncementTitle, etAnnouncementMessage;
    private Spinner spinnerType, spinnerPriority;
    private Button btnAddAnnouncement;
    private RecyclerView recyclerViewAnnouncements;

    private AnnouncementAdapter adapter;
    private List<AnnouncementModel> announcementList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Check authentication
            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            setContentView(R.layout.activity_announcement_management);

            db = FirebaseFirestore.getInstance();
            initializeViews();
            setupSpinners();
            loadAnnouncements();

            btnAddAnnouncement.setOnClickListener(v -> addAnnouncement());

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading page: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        etAnnouncementTitle = findViewById(R.id.etAnnouncementTitle);
        etAnnouncementMessage = findViewById(R.id.etAnnouncementMessage);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        btnAddAnnouncement = findViewById(R.id.btnAddAnnouncement);
        recyclerViewAnnouncements = findViewById(R.id.recyclerViewAnnouncements);
        announcementList = new ArrayList<>();
        adapter = new AnnouncementAdapter(announcementList);
        ImageView btnBack = findViewById(R.id.btnBack);
        Log.d(TAG, "Back button found: " + (btnBack != null));
        Log.d(TAG, "Back button clickable: " + (btnBack != null && btnBack.isClickable()));

        if (btnBack != null) {
            // Make absolutely sure it's clickable
            btnBack.setClickable(true);
            btnBack.setFocusable(true);

            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "========= BACK BUTTON CLICKED =========");
                    Toast.makeText(AnnouncementManagementActivity.this,
                            "Back button pressed!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            Log.d(TAG, "Back button listener attached successfully");
        } else {
            Log.e(TAG, "ERROR: Back button is NULL!");
        }


        recyclerViewAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnnouncements.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Type Spinner
        String[] types = {"info", "warning", "success", "event"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Priority Spinner (0-10)
        String[] priorities = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                priorities
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(5); // Default to priority 5
    }

    private void loadAnnouncements() {
        Log.d(TAG, "Loading announcements...");

        db.collection("announcements")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isFinishing() && !isDestroyed()) {
                        announcementList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                AnnouncementModel announcement = document.toObject(AnnouncementModel.class);
                                announcement.setId(document.getId());
                                announcementList.add(announcement);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing announcement: " + e.getMessage());
                            }
                        }

                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Loaded " + announcementList.size() + " announcements");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Log.e(TAG, "Error loading announcements: " + e.getMessage(), e);
                        Toast.makeText(this, "Error loading announcements: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addAnnouncement() {
        if (isFinishing() || isDestroyed()) return;

        String title = etAnnouncementTitle.getText().toString().trim();
        String message = etAnnouncementMessage.getText().toString().trim();

        // Validation
        if (title.isEmpty()) {
            etAnnouncementTitle.setError("Title is required");
            etAnnouncementTitle.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            etAnnouncementMessage.setError("Message is required");
            etAnnouncementMessage.requestFocus();
            return;
        }

        String type = spinnerType.getSelectedItem().toString();
        int priority = Integer.parseInt(spinnerPriority.getSelectedItem().toString());

        // Disable button while processing
        btnAddAnnouncement.setEnabled(false);
        btnAddAnnouncement.setText("Posting...");

        // Create announcement data
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("title", title);
        announcement.put("message", message);
        announcement.put("type", type);
        announcement.put("priority", priority);
        announcement.put("active", true);
        announcement.put("created_at", Timestamp.now());

        // Set expiry to 30 days from now
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        announcement.put("expires_at", new Timestamp(calendar.getTime()));

        // Add to Firestore
        db.collection("announcements")
                .add(announcement)
                .addOnSuccessListener(documentReference -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(this, "✅ Announcement posted successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Clear form
                        etAnnouncementTitle.setText("");
                        etAnnouncementMessage.setText("");
                        spinnerType.setSelection(0);
                        spinnerPriority.setSelection(5);

                        // Re-enable button
                        btnAddAnnouncement.setEnabled(true);
                        btnAddAnnouncement.setText("Post Announcement");

                        // Reload announcements
                        loadAnnouncements();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Log.e(TAG, "Error posting announcement: " + e.getMessage(), e);
                        Toast.makeText(this, "❌ Error posting announcement: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                        // Re-enable button
                        btnAddAnnouncement.setEnabled(true);
                        btnAddAnnouncement.setText("Post Announcement");
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        if (adapter != null) {
            loadAnnouncements();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");

        // Clean up references
        if (adapter != null) {
            adapter = null;
        }
        if (announcementList != null) {
            announcementList.clear();
        }
    }
}