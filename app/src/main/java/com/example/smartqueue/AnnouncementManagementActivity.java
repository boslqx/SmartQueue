package com.example.smartqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementManagementActivity extends AppCompatActivity {

    private EditText etAnnouncementTitle, etAnnouncementMessage;
    private Spinner spinnerType, spinnerPriority;
    private Button btnAddAnnouncement;
    private RecyclerView recyclerViewAnnouncements;

    private FirebaseFirestore db;
    private List<AnnouncementModel> announcementList;
    private AnnouncementAdapter announcementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_management);

        db = FirebaseFirestore.getInstance();
        announcementList = new ArrayList<>();

        initializeViews();
        setupSpinners();
        setupRecyclerView();
        loadAnnouncements();
        setupClickListeners();

        Toast.makeText(this, "Announcement Management", Toast.LENGTH_SHORT).show();
    }

    private void initializeViews() {
        etAnnouncementTitle = findViewById(R.id.etAnnouncementTitle);
        etAnnouncementMessage = findViewById(R.id.etAnnouncementMessage);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        btnAddAnnouncement = findViewById(R.id.btnAddAnnouncement);
        recyclerViewAnnouncements = findViewById(R.id.recyclerViewAnnouncements);
    }

    private void setupSpinners() {
        // Type spinner
        String[] types = {"info", "warning", "urgent", "maintenance"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Priority spinner (1-5)
        String[] priorities = {"1 - Lowest", "2 - Low", "3 - Medium", "4 - High", "5 - Highest"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        // Set default selections
        spinnerType.setSelection(0); // "info"
        spinnerPriority.setSelection(2); // "3 - Medium"
    }

    private void setupRecyclerView() {
        // Remove the delete listener parameter
        announcementAdapter = new AnnouncementAdapter(announcementList);
        recyclerViewAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnnouncements.setAdapter(announcementAdapter);
    }

    private void setupClickListeners() {
        btnAddAnnouncement.setOnClickListener(v -> addAnnouncement());
    }

    private void addAnnouncement() {
        String title = etAnnouncementTitle.getText().toString().trim();
        String message = etAnnouncementMessage.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString().split(" ")[0]; // Get just "info", "warning", etc.
        int priority = spinnerPriority.getSelectedItemPosition() + 1; // 1-5

        if (title.isEmpty()) {
            etAnnouncementTitle.setError("Title is required");
            return;
        }

        if (message.isEmpty()) {
            etAnnouncementMessage.setError("Message is required");
            return;
        }

        // Create announcement data matching your Firebase structure
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("active", true);
        announcement.put("created_at", System.currentTimeMillis());
        announcement.put("message", message);
        announcement.put("priority", priority);
        announcement.put("title", title);
        announcement.put("type", type);

        // Add to Firestore
        db.collection("announcements")
                .add(announcement)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Announcement posted successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadAnnouncements(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error posting announcement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAnnouncements() {
        db.collection("announcements")
                .orderBy("created_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    announcementList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AnnouncementModel announcement = document.toObject(AnnouncementModel.class);
                        announcement.setId(document.getId());
                        announcementList.add(announcement);
                    }
                    announcementAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading announcements: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteAnnouncement(int position) {
        if (position >= 0 && position < announcementList.size()) {
            AnnouncementModel announcement = announcementList.get(position);
            db.collection("announcements")
                    .document(announcement.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Announcement deleted", Toast.LENGTH_SHORT).show();
                        loadAnnouncements(); // Refresh list
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting announcement", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void clearForm() {
        etAnnouncementTitle.setText("");
        etAnnouncementMessage.setText("");
        spinnerType.setSelection(0);
        spinnerPriority.setSelection(2);
    }
}