package com.example.smartqueue;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClosedTimeSlotsActivity extends AppCompatActivity {

    private static final String TAG = "ClosedTimeSlots";

    private FirebaseFirestore db;
    private ImageView btnBack;
    private Button btnAddClosedSlot;
    private RecyclerView rvClosedSlots;
    private TextView tvEmptyState;

    private ClosedSlotAdapter adapter;
    private List<ClosedSlotModel> closedSlotList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.closed_timeslots_activity);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        loadClosedSlots();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddClosedSlot = findViewById(R.id.btnAddClosedSlot);
        rvClosedSlots = findViewById(R.id.rvClosedSlots);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        closedSlotList = new ArrayList<>();
        adapter = new ClosedSlotAdapter(closedSlotList, this::deleteClosedSlot);

        rvClosedSlots.setLayoutManager(new LinearLayoutManager(this));
        rvClosedSlots.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnAddClosedSlot.setOnClickListener(v -> showAddClosedSlotDialog());
    }

    private void loadClosedSlots() {
        db.collection("closed_slots")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isFinishing() && !isDestroyed()) {
                        closedSlotList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                ClosedSlotModel slot = document.toObject(ClosedSlotModel.class);
                                slot.setId(document.getId());
                                closedSlotList.add(slot);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing slot: " + e.getMessage());
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (closedSlotList.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvClosedSlots.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            rvClosedSlots.setVisibility(View.VISIBLE);
                        }

                        Log.d(TAG, "Loaded " + closedSlotList.size() + " closed slots");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Log.e(TAG, "Error loading slots: " + e.getMessage(), e);
                        Toast.makeText(this, "Error loading closed slots", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddClosedSlotDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_closed_slot, null);

        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etStartTime = dialogView.findViewById(R.id.etStartTime);
        EditText etEndTime = dialogView.findViewById(R.id.etEndTime);
        Spinner spinnerService = dialogView.findViewById(R.id.spinnerService);
        EditText etReason = dialogView.findViewById(R.id.etReason);

        // Setup service spinner
        String[] services = {"All Services", "discussion_room", "pool_table", "ping_pong", "music_room", "lecturer_consultation"};
        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, services);
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerService.setAdapter(serviceAdapter);

        // Date picker
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                etDate.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Closed Time Slot")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String date = etDate.getText().toString().trim();
                    String startTime = etStartTime.getText().toString().trim();
                    String endTime = etEndTime.getText().toString().trim();
                    String service = spinnerService.getSelectedItem().toString();
                    String reason = etReason.getText().toString().trim();

                    if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                        Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addClosedSlot(date, startTime, endTime, service, reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addClosedSlot(String date, String startTime, String endTime, String service, String reason) {
        Map<String, Object> closedSlot = new HashMap<>();
        closedSlot.put("date", date);
        closedSlot.put("start_time", startTime);
        closedSlot.put("end_time", endTime);
        closedSlot.put("service_type", service);
        closedSlot.put("reason", reason);
        closedSlot.put("created_at", Timestamp.now());

        db.collection("closed_slots")
                .add(closedSlot)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Closed slot added successfully", Toast.LENGTH_SHORT).show();
                    loadClosedSlots();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding slot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteClosedSlot(ClosedSlotModel slot) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Closed Slot")
                .setMessage("Are you sure you want to remove this closed slot?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("closed_slots").document(slot.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Closed slot removed", Toast.LENGTH_SHORT).show();
                                loadClosedSlots();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClosedSlots();
    }
}