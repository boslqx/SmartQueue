package com.example.smartqueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BookingManagementActivity extends AppCompatActivity {
    private RecyclerView recyclerViewBookings;
    private Spinner spinnerFilter;
    private AdminBookingAdapter adminBookingAdapter;
    private List<BookingModel> bookingList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_management);

        db = FirebaseFirestore.getInstance();
        bookingList = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupFilterSpinner();
        loadBookings();
    }

    private void initializeViews() {
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        spinnerFilter = findViewById(R.id.spinnerFilter);
    }

    private void setupRecyclerView() {
        adminBookingAdapter = new AdminBookingAdapter(this, bookingList);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBookings.setAdapter(adminBookingAdapter);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.booking_status_filter,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadBookings(); // Reload with filter
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void loadBookings() {
        String filter = spinnerFilter.getSelectedItem().toString();

        if ("All".equals(filter)) {
            db.collection("bookings")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            bookingList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                BookingModel booking = document.toObject(BookingModel.class);
                                booking.setDocumentId(document.getId());
                                bookingList.add(booking);
                            }
                            adminBookingAdapter.notifyDataSetChanged();
                        }
                    });
        } else {
            db.collection("bookings")
                    .whereEqualTo("status", filter.toLowerCase())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            bookingList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                BookingModel booking = document.toObject(BookingModel.class);
                                booking.setDocumentId(document.getId());
                                bookingList.add(booking);
                            }
                            adminBookingAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }
}