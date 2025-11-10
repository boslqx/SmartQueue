package com.example.smartqueue;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueStatusActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ImageView btnBack;
    private EditText etSearch;
    private Spinner spinnerFilter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;

    private BookingAdapter adapter;
    private List<BookingModel> allBookingsList;
    private List<BookingModel> filteredBookingsList;

    private String currentFilter = "all"; // all, confirmed, expired, cancelled, completed

    // Pagination variables
    private static final int PAGE_SIZE = 20;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private QueryDocumentSnapshot lastDocument = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.queue_status_activity);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews();
        setupRecyclerView();
        setupSearchAndFilter();
        loadBookings();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        recyclerView = findViewById(R.id.recyclerViewBookings);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        allBookingsList = new ArrayList<>();
        filteredBookingsList = new ArrayList<>();
        adapter = new BookingAdapter(this, filteredBookingsList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check if scrolled to bottom
                if (!isLoading && hasMoreData) {
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (manager != null) {
                        int visibleItemCount = manager.getChildCount();
                        int totalItemCount = manager.getItemCount();
                        int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();

                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                            // Load more when 5 items from bottom
                            loadMoreBookings();
                        }
                    }
                }
            }
        });
    }

    private void setupSearchAndFilter() {
        // Setup filter spinner
        String[] filters = {"All Bookings", "Confirmed", "Expired", "Cancelled", "Completed"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: currentFilter = "all"; break;
                    case 1: currentFilter = "confirmed"; break;
                    case 2: currentFilter = "expired"; break;
                    case 3: currentFilter = "cancelled"; break;
                    case 4: currentFilter = "completed"; break;
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup search with null check
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call applyFilters safely
                if (allBookingsList != null && filteredBookingsList != null) {
                    applyFilters();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadBookings() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Please log in to view bookings", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        isLoading = true;

        Query query = db.collection("bookings")
                .whereEqualTo("user_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allBookingsList.clear();
                    List<String> expiredBookingIds = new ArrayList<>();

                    if (queryDocumentSnapshots.size() < PAGE_SIZE) {
                        hasMoreData = false;
                    } else {
                        hasMoreData = true;
                        lastDocument = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingModel booking = createBookingFromDocument(document);
                        allBookingsList.add(booking);

                        // Check if booking should be marked as expired
                        if (booking.isExpired() && "confirmed".equalsIgnoreCase(booking.getStatus())) {
                            expiredBookingIds.add(document.getId());
                        }
                    }

                    // Update expired bookings in Firestore
                    updateExpiredBookings(expiredBookingIds);

                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    isLoading = false;
                    Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMoreBookings() {
        if (isLoading || !hasMoreData || lastDocument == null) return;

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        isLoading = true;
        Toast.makeText(this, "Loading more...", Toast.LENGTH_SHORT).show();

        db.collection("bookings")
                .whereEqualTo("user_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .startAfter(lastDocument)
                .limit(PAGE_SIZE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> expiredBookingIds = new ArrayList<>();

                    if (queryDocumentSnapshots.size() < PAGE_SIZE) {
                        hasMoreData = false;
                    } else if (!queryDocumentSnapshots.isEmpty()) {
                        lastDocument = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingModel booking = createBookingFromDocument(document);
                        allBookingsList.add(booking);

                        if (booking.isExpired() && "confirmed".equalsIgnoreCase(booking.getStatus())) {
                            expiredBookingIds.add(document.getId());
                        }
                    }

                    updateExpiredBookings(expiredBookingIds);
                    isLoading = false;
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                    Toast.makeText(this, "Failed to load more", Toast.LENGTH_SHORT).show();
                });
    }

    private BookingModel createBookingFromDocument(QueryDocumentSnapshot document) {
        Long durationLong = document.getLong("duration");
        int duration = (durationLong != null) ? durationLong.intValue() : 1;

        Double amountDouble = document.getDouble("amount");
        double amount = (amountDouble != null) ? amountDouble : 0.0;

        BookingModel booking = new BookingModel();
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
        booking.setDuration(duration);
        booking.setAmount(amount);
        booking.setPaymentStatus(document.getString("payment_status"));
        booking.setStatus(document.getString("status"));
        booking.setCreatedAt(document.getTimestamp("created_at"));
        booking.setUpdatedAt(document.getTimestamp("updated_at"));

        return booking;
    }

    private void updateExpiredBookings(List<String> expiredBookingIds) {
        if (expiredBookingIds.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "expired");
        updates.put("updated_at", Timestamp.now());

        for (String bookingId : expiredBookingIds) {
            db.collection("bookings").document(bookingId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Update local list
                        for (BookingModel booking : allBookingsList) {
                            if (booking.getDocumentId().equals(bookingId)) {
                                booking.setStatus("expired");
                                booking.setUpdatedAt(Timestamp.now());
                                break;
                            }
                        }
                    });
        }
    }

    private void applyFilters() {
        // Safety check
        if (allBookingsList == null || filteredBookingsList == null ||
                adapter == null || etSearch == null) {
            return;
        }

        try {
            String searchQuery = etSearch.getText().toString().toLowerCase().trim();
            filteredBookingsList.clear();

            for (BookingModel booking : allBookingsList) {
                if (booking == null) continue;

                String computedStatus = booking.getComputedStatus();
                if (computedStatus == null) continue;

                // Apply status filter
                boolean statusMatch = currentFilter.equals("all") ||
                        computedStatus.equalsIgnoreCase(currentFilter);

                // Apply search filter
                boolean searchMatch = searchQuery.isEmpty() ||
                        (booking.getServiceName() != null && booking.getServiceName().toLowerCase().contains(searchQuery)) ||
                        (booking.getLocationId() != null && booking.getLocationId().toLowerCase().contains(searchQuery)) ||
                        (booking.getDate() != null && booking.getDate().contains(searchQuery)) ||
                        computedStatus.toLowerCase().contains(searchQuery);

                if (statusMatch && searchMatch) {
                    filteredBookingsList.add(booking);
                }
            }

            // Update UI on main thread
            runOnUiThread(() -> {
                if (filteredBookingsList.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    layoutEmptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            Log.e("QueueStatus", "Error in applyFilters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bookings when returning to this activity
        loadBookings();
    }
}