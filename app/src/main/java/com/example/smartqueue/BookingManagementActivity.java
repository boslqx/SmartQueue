package com.example.smartqueue;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BookingManagementActivity extends AppCompatActivity {

    private static final String TAG = "BookingManagement";

    private FirebaseFirestore db;
    private RecyclerView recyclerViewBookings;
    private Spinner spinnerFilter;
    private AdminBookingAdapter adapter;
    private List<BookingModel> bookingList;
    private List<BookingModel> allBookings;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_management);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupSpinner();

        // CALL THE CORRECT METHOD
        loadBookingsDirect();
    }

    private void initializeViews() {
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        bookingList = new ArrayList<>();
        allBookings = new ArrayList<>();

        adapter = new AdminBookingAdapter(this, bookingList);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBookings.setAdapter(adapter);
    }

    private void setupSpinner() {
        String[] filterOptions = {"All Bookings", "Pending", "Confirmed", "Cancelled", "Completed"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterBookings(filterOptions[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    public void loadBookingsDirect() {
        Log.d(TAG, "=== LOADING ALL BOOKINGS FOR ADMIN ===");

        db.collection("bookings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allBookings.clear();
                        bookingList.clear();

                        int documentCount = task.getResult().size();
                        Log.d(TAG, "=== QUERY SUCCESS: Found " + documentCount + " booking documents ===");

                        if (documentCount == 0) {
                            Log.w(TAG, "=== NO BOOKINGS FOUND IN DATABASE ===");
                            showEmptyState("No bookings in system");
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Log.d(TAG, "Processing booking: " + document.getId());
                                Log.d(TAG, "RAW DATA: " + document.getData());

                                // Create booking object
                                BookingModel booking = documentToBookingModel(document);

                                if (booking != null) {
                                    allBookings.add(booking);
                                    Log.d(TAG, "✓ Added: " + booking.getServiceName() +
                                            " | User: " + booking.getUserName() +
                                            " | Status: " + booking.getStatus());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error with document " + document.getId() + ": " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "=== FINAL: Loaded " + allBookings.size() + " bookings ===");

                        // Show all bookings by default
                        filterBookings("All Bookings");

                    } else {
                        Log.e(TAG, "=== FIRESTORE ERROR: " + task.getException());
                        showEmptyState("Database error: " + task.getException().getMessage());
                    }
                });
    }

    private BookingModel documentToBookingModel(QueryDocumentSnapshot document) {
        try {
            BookingModel booking = new BookingModel();
            booking.setDocumentId(document.getId());

            // Map all fields
            booking.setUserId(document.getString("user_id"));
            booking.setUserEmail(document.getString("user_email"));
            booking.setUserName(document.getString("user_name"));
            booking.setServiceType(document.getString("serviceType"));
            booking.setServiceName(document.getString("serviceName"));
            booking.setLocationId(document.getString("locationId"));
            booking.setStartTime(document.getString("startTime"));
            booking.setEndTime(document.getString("endTime"));
            booking.setStatus(document.getString("status"));
            booking.setPaymentStatus(document.getString("payment_status"));

            // Handle amount
            if (document.getDouble("amount") != null) {
                booking.setAmount(document.getDouble("amount"));
            } else if (document.getDouble("price") != null) {
                booking.setAmount(document.getDouble("price"));
            } else {
                booking.setAmount(0.0);
            }

            // Handle date from timestamp
            if (document.getTimestamp("created_at") != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                String extractedDate = sdf.format(document.getTimestamp("created_at").toDate());
                booking.setDate(extractedDate);
            } else {
                booking.setDate("Unknown date");
            }

            // Handle timestamps
            booking.setCreatedAt(document.getTimestamp("created_at"));
            booking.setUpdatedAt(document.getTimestamp("updated_at"));

            return booking;

        } catch (Exception e) {
            Log.e(TAG, "Error converting document to BookingModel: " + e.getMessage());
            return null;
        }
    }

    private void filterBookings(String filter) {
        bookingList.clear();
        Log.d(TAG, "Applying filter: " + filter + " to " + allBookings.size() + " total bookings");

        for (BookingModel booking : allBookings) {
            String status = booking.getStatus();
            Log.d(TAG, "Checking: " + booking.getServiceName() + " | Status: " + status);

            switch (filter) {
                case "All Bookings":
                    bookingList.add(booking);
                    break;
                case "Pending":
                    if ("pending".equalsIgnoreCase(status)) {
                        bookingList.add(booking);
                    }
                    break;
                case "Confirmed":
                    if ("confirmed".equalsIgnoreCase(status)) {
                        bookingList.add(booking);
                    }
                    break;
                case "Cancelled":
                    if ("cancelled".equalsIgnoreCase(status)) {
                        bookingList.add(booking);
                    }
                    break;
                case "Completed":
                    if ("completed".equalsIgnoreCase(status)) {
                        bookingList.add(booking);
                    }
                    break;
            }
        }

        adapter.notifyDataSetChanged();
        Log.d(TAG, "Filter result: " + bookingList.size() + " bookings for '" + filter + "'");

        // Update empty state
        if (bookingList.isEmpty()) {
            showEmptyState("No " + filter.toLowerCase() + " bookings");
        } else {
            hideEmptyState();
        }
    }

    private void showEmptyState(String message) {
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewBookings.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewBookings.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookingsDirect();
    }
}