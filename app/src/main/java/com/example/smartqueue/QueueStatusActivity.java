package com.example.smartqueue;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QueueStatusActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ImageView btnBack;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState; // Changed from TextView to LinearLayout
    private TextView tvEmptyStateText; // Added for the actual text view
    private BookingAdapter adapter;
    private List<BookingModel> bookingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.queue_status_activity);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews();
        setupRecyclerView();
        loadBookings();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewBookings);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.tvEmptyState); // This is the LinearLayout container
        // If you need to access the text view inside, you can add:
        // tvEmptyStateText = layoutEmptyState.findViewById(R.id.text_view_id_inside_empty_state);

        // Note: Since you don't have an ID for the TextView inside the empty state layout,
        // you might want to add one in your XML, or just use the layoutEmptyState for visibility control
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(this, bookingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
        layoutEmptyState.setVisibility(View.GONE); // Use layoutEmptyState instead of tvEmptyState

        db.collection("bookings")
                .whereEqualTo("user_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Handle potential null values safely
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

                        bookingList.add(booking);
                    }

                    progressBar.setVisibility(View.GONE);

                    if (bookingList.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE); // Show the empty state layout
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmptyState.setVisibility(View.GONE); // Hide the empty state layout
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE); // Show empty state on error
                    // If you want to show error message, you'll need to add a TextView for errors
                    Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bookings when returning from details
        loadBookings();
    }
}