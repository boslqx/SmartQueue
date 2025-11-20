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
import java.util.ArrayList;
import java.util.Arrays;
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
        // Load from bookings collection with status = "cancelled" OR "closed"
        db.collection("bookings")
                .whereIn("status", Arrays.asList("cancelled", "closed"))
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isFinishing() && !isDestroyed()) {
                        closedSlotList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                ClosedSlotModel slot = new ClosedSlotModel();
                                slot.setId(document.getId());
                                slot.setDate(document.getString("date"));
                                slot.setStart_time(document.getString("start_time"));
                                slot.setEnd_time(document.getString("end_time"));
                                slot.setService_type(document.getString("service_name"));

                                // Add location info
                                String location = document.getString("location_id");
                                if (location != null && !location.isEmpty()) {
                                    slot.setService_type(slot.getService_type() + " - " + location);
                                }

                                slot.setReason(document.getString("notes"));
                                slot.setCreated_at(document.getTimestamp("created_at"));
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
        String[] services = {
                "Discussion Room - All",
                "Discussion Room - Room L1",
                "Discussion Room - Room L2",
                "Discussion Room - Room S1",
                "Discussion Room - Room S2",
                "Discussion Room - Room S3",
                "Pool Table - All",
                "Pool Table - Pool Table 1",
                "Pool Table - Pool Table 2",
                "Ping Pong - All",
                "Ping Pong - Table 1",
                "Ping Pong - Table 2",
                "Music Room"
        };

        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                services
        );
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
                    String selectedService = spinnerService.getSelectedItem().toString();
                    String reason = etReason.getText().toString().trim();

                    if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                        Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (reason.isEmpty()) {
                        reason = "Closed for maintenance";
                    }

                    addClosedSlot(date, startTime, endTime, selectedService, reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addClosedSlot(String date, String startTime, String endTime, String selectedService, String reason) {
        // Parse the selected service
        String[] parts = selectedService.split(" - ");
        String serviceName = parts[0];
        String location = parts.length > 1 ? parts[1] : null;

        // Get service type for database
        String serviceType = getServiceTypeFromName(serviceName);

        // Get locations to close
        List<String> locationsToClose = getLocationsToClose(serviceName, location);

        int totalSlots = locationsToClose.size();
        final int[] successCount = {0};
        final int[] failCount = {0};

        for (String loc : locationsToClose) {
            Map<String, Object> closedBooking = new HashMap<>();
            closedBooking.put("date", date);
            closedBooking.put("start_time", startTime);
            closedBooking.put("end_time", endTime);
            closedBooking.put("service_type", serviceType);
            closedBooking.put("service_name", serviceName);
            closedBooking.put("location_id", loc);
            closedBooking.put("status", "closed");
            closedBooking.put("user_id", "SYSTEM");
            closedBooking.put("user_name", "System Maintenance");
            closedBooking.put("user_email", "system@smartqueue.com");
            closedBooking.put("notes", reason);
            closedBooking.put("created_at", Timestamp.now());
            closedBooking.put("updated_at", Timestamp.now());
            closedBooking.put("duration", 1);
            closedBooking.put("amount", 0.0);
            closedBooking.put("payment_status", "free");

            Log.d(TAG, "Creating closed slot: " + closedBooking.toString());

            db.collection("bookings")
                    .add(closedBooking)
                    .addOnSuccessListener(documentReference -> {
                        successCount[0]++;
                        Log.d(TAG, "Closed slot added: " + documentReference.getId() + " for " + loc);

                        if (successCount[0] + failCount[0] == totalSlots) {
                            showCompletionMessage(successCount[0], failCount[0]);
                        }
                    })
                    .addOnFailureListener(e -> {
                        failCount[0]++;
                        Log.e(TAG, "Error adding closed slot for " + loc + ": " + e.getMessage());

                        if (successCount[0] + failCount[0] == totalSlots) {
                            showCompletionMessage(successCount[0], failCount[0]);
                        }
                    });
        }
    }

    private void showCompletionMessage(int successCount, int failCount) {
        if (failCount == 0) {
            Toast.makeText(this, successCount + " closed slot(s) added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, successCount + " succeeded, " + failCount + " failed", Toast.LENGTH_LONG).show();
        }
        loadClosedSlots();
    }

    private String getServiceTypeFromName(String serviceName) {
        switch (serviceName) {
            case "Discussion Room":
                return "discussion_room";
            case "Pool Table":
                return "pool_table";
            case "Ping Pong":
                return "ping_pong";
            case "Music Room":
                return "music_room";
            default:
                return serviceName.toLowerCase().replace(" ", "_");
        }
    }

    private List<String> getLocationsToClose(String serviceName, String specificLocation) {
        List<String> locations = new ArrayList<>();

        if (specificLocation != null && !specificLocation.equals("All")) {
            // Close only the specific location
            locations.add(specificLocation);
            return locations;
        }

        // Close all locations for this service
        switch (serviceName) {
            case "Discussion Room":
                locations.addAll(Arrays.asList("Room L1", "Room L2", "Room S1", "Room S2", "Room S3"));
                break;
            case "Pool Table":
                locations.addAll(Arrays.asList("Pool Table 1", "Pool Table 2"));
                break;
            case "Ping Pong":
                locations.addAll(Arrays.asList("Table 1", "Table 2"));
                break;
            case "Music Room":
                locations.add("Music Room");
                break;
            default:
                locations.add(serviceName);
                break;
        }

        return locations;
    }

    private void deleteClosedSlot(ClosedSlotModel slot) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Closed Slot")
                .setMessage("Are you sure you want to remove this closed slot?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("bookings").document(slot.getId())
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