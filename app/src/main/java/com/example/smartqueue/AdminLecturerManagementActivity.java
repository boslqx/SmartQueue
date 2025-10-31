package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminLecturerManagementActivity extends AppCompatActivity {

    private static final String TAG = "AdminLecturerMgmt";
    private FirebaseFirestore db;

    private ImageView btnBack;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddLecturer;

    private AdminLecturerAdapter adapter;
    private List<LecturerModel> lecturerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_lecturer_management_activity);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        loadLecturers();

        btnBack.setOnClickListener(v -> finish());
        fabAddLecturer.setOnClickListener(v -> navigateToAddLecturer());
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewLecturers);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddLecturer = findViewById(R.id.fabAddLecturer);
    }

    private void setupRecyclerView() {
        lecturerList = new ArrayList<>();
        adapter = new AdminLecturerAdapter(lecturerList, new AdminLecturerAdapter.OnLecturerActionListener() {
            @Override
            public void onEdit(LecturerModel lecturer) {
                navigateToEditLecturer(lecturer);
            }

            @Override
            public void onDelete(LecturerModel lecturer) {
                showDeleteConfirmation(lecturer);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadLecturers() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("lecturers")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    lecturerList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        LecturerModel lecturer = document.toObject(LecturerModel.class);
                        lecturer.setId(document.getId());

                        // Get booked hours count
                        Long bookedHours = document.getLong("booked_hours");
                        if (bookedHours != null) {
                            lecturer.setBooked_hours(bookedHours.intValue());
                        }

                        lecturerList.add(lecturer);
                    }

                    progressBar.setVisibility(View.GONE);

                    if (lecturerList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }

                    Log.d(TAG, "Loaded " + lecturerList.size() + " lecturers");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Error loading lecturers");
                    Toast.makeText(this, "Failed to load lecturers: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading lecturers", e);
                });
    }

    private void navigateToAddLecturer() {
        Intent intent = new Intent(this, AddEditLecturerActivity.class);
        startActivity(intent);
    }

    private void navigateToEditLecturer(LecturerModel lecturer) {
        Intent intent = new Intent(this, AddEditLecturerActivity.class);
        intent.putExtra("lecturer_id", lecturer.getId());
        intent.putExtra("lecturer_name", lecturer.getName());
        intent.putExtra("lecturer_email", lecturer.getEmail());
        intent.putExtra("lecturer_department", lecturer.getDepartment());
        intent.putExtra("lecturer_office", lecturer.getOffice_location());
        intent.putExtra("lecturer_weekly_hours", lecturer.getWeekly_hours());
        startActivity(intent);
    }

    private void showDeleteConfirmation(LecturerModel lecturer) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Lecturer")
                .setMessage("Are you sure you want to delete " + lecturer.getName() + "?\n\n" +
                        "This will also cancel all their upcoming consultations.")
                .setPositiveButton("Delete", (dialog, which) -> deleteLecturer(lecturer))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteLecturer(LecturerModel lecturer) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("lecturers").document(lecturer.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Also cancel all upcoming bookings for this lecturer
                    cancelLecturerBookings(lecturer.getId());

                    Toast.makeText(this, "Lecturer deleted successfully", Toast.LENGTH_SHORT).show();
                    loadLecturers();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to delete lecturer: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting lecturer", e);
                });
    }

    private void cancelLecturerBookings(String lecturerId) {
        db.collection("bookings")
                .whereEqualTo("lecturer_id", lecturerId)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("status", "cancelled");
                    }
                    Log.d(TAG, "Cancelled " + queryDocumentSnapshots.size() + " bookings");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cancelling bookings", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLecturers();
    }
}