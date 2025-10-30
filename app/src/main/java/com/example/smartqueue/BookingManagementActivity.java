package com.example.smartqueue;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BookingManagementActivity extends AppCompatActivity {

    private static final String TAG = "BookingManagement";

    private FirebaseFirestore db;
    private ImageView btnBack;
    private Spinner spinnerFilter;
    private RecyclerView recyclerViewBookings;
    private TextView tvEmptyState;
    private ProgressBar progressBar;

    private AdminBookingAdapter adapter;
    private List<BookingModel> allBookingsList;
    private List<BookingModel> filteredBookingsList;

    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_management);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupFilterSpinner();
        loadBookings();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);

        allBookingsList = new ArrayList<>();
        filteredBookingsList = new ArrayList<>();

        adapter = new AdminBookingAdapter(this, filteredBookingsList);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBookings.setAdapter(adapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupFilterSpinner() {
        String[] filters = {"All", "Confirmed", "Pending", "Cancelled", "Completed"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filters
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                filterBookings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadBookings() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        recyclerViewBookings.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        Log.d(TAG, "Loading all bookings from Firestore...");

        db.collection("bookings")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isFinishing() && !isDestroyed()) {
                        allBookingsList.clear();

                        Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " bookings");

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                BookingModel booking = new BookingModel();

                                // Map Firestore fields to BookingModel
                                booking.setDocumentId(document.getId());
                                booking.setUserId(document.getString("user_id"));
                                booking.setUserEmail(document.getString("user_email"));
                                booking.setUserName(document.getString("user_name"));
                                booking.setServiceType(document.getString("service_type"));
                                booking.setServiceName(document.getString("service_name"));
                                booking.setLocationId(document.getString("location_id"));
                                booking.setDate(document.getString("date"));
                                booking.setStartTime(document.getString("start_time"));
                                booking.setEndTime(document.getString("end_time"));

                                Long durationLong = document.getLong("duration");
                                booking.setDuration(durationLong != null ? durationLong.intValue() : 1);

                                Double amountDouble = document.getDouble("amount");
                                booking.setAmount(amountDouble != null ? amountDouble : 0.0);

                                booking.setPaymentStatus(document.getString("payment_status"));
                                booking.setStatus(document.getString("status"));
                                booking.setCreatedAt(document.getTimestamp("created_at"));
                                booking.setUpdatedAt(document.getTimestamp("updated_at"));

                                allBookingsList.add(booking);

                                Log.d(TAG, "Loaded booking: " + booking.getServiceName() +
                                        " - " + booking.getStatus());

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing booking: " + e.getMessage(), e);
                            }
                        }

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        // Apply current filter
                        filterBookings();

                        Log.d(TAG, "Successfully loaded " + allBookingsList.size() + " bookings");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Log.e(TAG, "Error loading bookings: " + e.getMessage(), e);

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Error loading bookings: " + e.getMessage());
                    }
                });
    }

    private void filterBookings() {
        filteredBookingsList.clear();

        if ("All".equals(currentFilter)) {
            filteredBookingsList.addAll(allBookingsList);
        } else {
            for (BookingModel booking : allBookingsList) {
                if (booking.getStatus() != null &&
                        booking.getStatus().equalsIgnoreCase(currentFilter)) {
                    filteredBookingsList.add(booking);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredBookingsList.isEmpty()) {
            recyclerViewBookings.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No " + currentFilter.toLowerCase() + " bookings found");
        } else {
            recyclerViewBookings.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }

        Log.d(TAG, "Filtered bookings: " + filteredBookingsList.size() +
                " (filter: " + currentFilter + ")");
    }

    // Public method for adapter to refresh data
    public void loadBookingsDirect() {
        loadBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bookings when returning from detail view
        loadBookings();
    }
}