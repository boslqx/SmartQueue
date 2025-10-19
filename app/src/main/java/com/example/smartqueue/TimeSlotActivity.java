package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimeSlotActivity extends AppCompatActivity {

    private static final String TAG = "TimeSlotActivity";
    private FirebaseFirestore db;

    private String serviceType, locationId, serviceName, extraInfo;
    private String availableFrom, availableTo;
    private int maxDuration;
    private boolean isPaid;
    private double price;

    private TextView tvServiceTitle, tvLocation;
    private DatePicker datePicker;
    private RecyclerView rvTimeSlots;
    private Button btnConfirmBooking;

    private List<TimeSlotModel> timeSlots = new ArrayList<>();
    private TimeSlotAdapter adapter;
    private String selectedDate;
    private TimeSlotModel selectedSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_slot_activity);

        initializeViews();
        getIntentData();
        setupRecyclerView();
        setupDatePicker();
        setupButton();
    }

    private void initializeViews() {
        db = FirebaseFirestore.getInstance();

        tvServiceTitle = findViewById(R.id.tvServiceTitle);
        tvLocation = findViewById(R.id.tvLocation);
        datePicker = findViewById(R.id.datePicker);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        serviceType = intent.getStringExtra("serviceType");
        serviceName = intent.getStringExtra("serviceName");
        locationId = intent.getStringExtra("locationId");
        extraInfo = intent.getStringExtra("extraInfo");
        availableFrom = intent.getStringExtra("availableFrom");
        availableTo = intent.getStringExtra("availableTo");
        maxDuration = intent.getIntExtra("maxDuration", 1);
        isPaid = intent.getBooleanExtra("isPaid", false);
        price = intent.getDoubleExtra("price", 0.0);

        // Update UI
        tvServiceTitle.setText(serviceName);
        tvLocation.setText(locationId + (extraInfo != null ? " (" + extraInfo + ")" : ""));

        Log.d(TAG, "Service: " + serviceType + ", Location: " + locationId);
        Log.d(TAG, "Available: " + availableFrom + " - " + availableTo);
    }

    private void setupRecyclerView() {
        adapter = new TimeSlotAdapter(timeSlots, position -> {
            // Handle slot selection
            selectedSlot = timeSlots.get(position);

            // Deselect all other slots
            for (TimeSlotModel slot : timeSlots) {
                slot.setSelected(false);
            }

            // Select current slot
            selectedSlot.setSelected(true);
            adapter.notifyDataSetChanged();

            btnConfirmBooking.setEnabled(true);
        });

        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 2));
        rvTimeSlots.setAdapter(adapter);
    }

    private void setupDatePicker() {
        // Set minimum date to today
        Calendar calendar = Calendar.getInstance();
        datePicker.setMinDate(calendar.getTimeInMillis());

        // Set initial selected date
        selectedDate = getSelectedDate();

        datePicker.setOnDateChangedListener((view, year, month, day) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            loadTimeSlots();
        });

        // Load slots for initial date
        loadTimeSlots();
    }

    private void setupButton() {
        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private String getSelectedDate() {
        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1; // Month is 0-based
        int day = datePicker.getDayOfMonth();
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }

    private void loadTimeSlots() {
        Log.d(TAG, "Loading time slots for: " + selectedDate);

        // Generate all possible time slots first
        generateTimeSlots();

        // Then fetch booked slots from Firestore and mark them as unavailable
        fetchBookedSlots();
    }

    private void generateTimeSlots() {
        timeSlots.clear();

        try {
            // Parse available time range
            int startHour = Integer.parseInt(availableFrom.split(":")[0]);
            int endHour = Integer.parseInt(availableTo.split(":")[0]);

            // Generate hourly slots
            for (int hour = startHour; hour < endHour; hour++) {
                String startTime = String.format(Locale.getDefault(), "%02d:00", hour);
                String endTime = String.format(Locale.getDefault(), "%02d:00", hour + 1);
                String timeRange = startTime + " - " + endTime;

                TimeSlotModel slot = new TimeSlotModel(timeRange, startTime, endTime, true);
                timeSlots.add(slot);
            }

            adapter.notifyDataSetChanged();
            Log.d(TAG, "Generated " + timeSlots.size() + " time slots");

        } catch (Exception e) {
            Log.e(TAG, "Error generating time slots: " + e.getMessage());
            Toast.makeText(this, "Error generating time slots", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchBookedSlots() {
        db.collection("bookings")
                .whereEqualTo("serviceType", serviceType)
                .whereEqualTo("locationId", locationId)
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " existing bookings");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String bookedStartTime = document.getString("startTime");
                        String bookedEndTime = document.getString("endTime");

                        // Mark overlapping slots as unavailable
                        markSlotsAsBooked(bookedStartTime, bookedEndTime);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching bookings: " + e.getMessage());
                    Toast.makeText(this, "Error loading availability", Toast.LENGTH_SHORT).show();
                });
    }

    private void markSlotsAsBooked(String bookedStart, String bookedEnd) {
        for (TimeSlotModel slot : timeSlots) {
            if (isTimeOverlap(slot.getStartTime(), slot.getEndTime(), bookedStart, bookedEnd)) {
                slot.setAvailable(false);
                Log.d(TAG, "Marked slot as booked: " + slot.getTimeRange());
            }
        }
    }

    private boolean isTimeOverlap(String slotStart, String slotEnd, String bookedStart, String bookedEnd) {
        // Simple string comparison for HH:MM format
        return (slotStart.compareTo(bookedEnd) < 0 && slotEnd.compareTo(bookedStart) > 0);
    }

    private void confirmBooking() {
        if (selectedSlot == null) {
            Toast.makeText(this, R.string.please_select_time_slot, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(TimeSlotActivity.this, ConfirmBookingActivity.class);
        intent.putExtra("serviceType", serviceType);
        intent.putExtra("serviceName", serviceName);
        intent.putExtra("locationId", locationId);
        intent.putExtra("extraInfo", extraInfo);
        intent.putExtra("date", selectedDate);
        intent.putExtra("startTime", selectedSlot.getStartTime());
        intent.putExtra("endTime", selectedSlot.getEndTime());
        intent.putExtra("isPaid", isPaid);
        intent.putExtra("price", price);
        startActivity(intent);
    }
}