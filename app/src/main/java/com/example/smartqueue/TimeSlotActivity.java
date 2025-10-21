package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimeSlotActivity extends AppCompatActivity {

    private static final String TAG = "TimeSlotActivity";
    private static final int MAX_BOOKING_DAYS = 7; // Allow booking up to 7 days in advance

    private FirebaseFirestore db;
    private String serviceType, locationId, serviceName, extraInfo;
    private String availableFrom, availableTo;
    private int maxDuration;
    private boolean isPaid;
    private double price;

    private TextView tvServiceTitle, tvLocation, tvSelectedDuration, tvSelectedDate;
    private RecyclerView rvDates, rvTimeSlots;
    private Button btnConfirmBooking;
    private ImageView btnBack;

    private List<DateItemModel> dateList = new ArrayList<>();
    private List<TimeSlotModel> timeSlots = new ArrayList<>();
    private DateAdapter dateAdapter;
    private TimeSlotAdapter timeSlotAdapter;
    private String selectedDate;
    private List<TimeSlotModel> selectedSlots = new ArrayList<>();
    private int currentSelectedDuration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_slot_activity);

        initializeViews();
        getIntentData();
        setupDateList();
        setupDateRecyclerView();
        setupTimeSlotRecyclerView();
        setupButton();
        setupBackButton();
        updateDurationText();
    }

    private void initializeViews() {
        db = FirebaseFirestore.getInstance();
        tvServiceTitle = findViewById(R.id.tvServiceTitle);
        tvLocation = findViewById(R.id.tvLocation);
        tvSelectedDuration = findViewById(R.id.tvSelectedDuration);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        rvDates = findViewById(R.id.rvDates);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        btnBack = findViewById(R.id.btnBack);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        serviceType = intent.getStringExtra("serviceType");
        serviceName = intent.getStringExtra("serviceName");
        locationId = intent.getStringExtra("locationId");
        extraInfo = intent.getStringExtra("extraInfo");
        availableFrom = intent.getStringExtra("availableFrom");
        availableTo = intent.getStringExtra("availableTo");

        // Music room can book up to 3 hours, others only 1 hour
        if ("music_room".equalsIgnoreCase(serviceType)) {
            maxDuration = 3;
        } else {
            maxDuration = 1;
        }

        isPaid = intent.getBooleanExtra("isPaid", false);
        price = intent.getDoubleExtra("price", 0.0);

        // Update UI
        tvServiceTitle.setText(serviceName);
        tvLocation.setText(locationId + (extraInfo != null ? " (" + extraInfo + ")" : ""));
        Log.d(TAG, "Service: " + serviceType + ", Location: " + locationId);
        Log.d(TAG, "Available: " + availableFrom + " - " + availableTo + ", Max Duration: " + maxDuration + " hours");
    }

    private void setupDateList() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        for (int i = 0; i < MAX_BOOKING_DAYS; i++) {
            String date = dateFormat.format(calendar.getTime());
            String dayOfWeek = dayOfWeekFormat.format(calendar.getTime()).toUpperCase();
            String dayOfMonth = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String monthYear = monthYearFormat.format(calendar.getTime());
            boolean isToday = (i == 0);

            DateItemModel dateItem = new DateItemModel(date, dayOfWeek, dayOfMonth, monthYear, isToday);

            // Select today by default
            if (i == 0) {
                dateItem.setSelected(true);
                selectedDate = date;
                updateSelectedDateDisplay(date);
            }

            dateList.add(dateItem);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void setupDateRecyclerView() {
        dateAdapter = new DateAdapter(dateList, (date, position) -> {
            selectedDate = date;
            updateSelectedDateDisplay(date);
            resetTimeSlotSelection();
            loadTimeSlots();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvDates.setLayoutManager(layoutManager);
        rvDates.setAdapter(dateAdapter);

        // Load initial time slots for today
        loadTimeSlots();
    }

    private void updateSelectedDateDisplay(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            String formattedDate = outputFormat.format(inputFormat.parse(date));
            tvSelectedDate.setText(formattedDate);
        } catch (Exception e) {
            tvSelectedDate.setText(date);
        }
    }

    private void setupTimeSlotRecyclerView() {
        timeSlotAdapter = new TimeSlotAdapter(timeSlots, new TimeSlotAdapter.OnTimeSlotClickListener() {
            @Override
            public void onTimeSlotClick(int position) {
                handleSlotSelection(position);
            }

            @Override
            public void onSelectionChanged(int selectedDuration, int maxDuration) {
                // Handled locally
            }
        }, maxDuration);

        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 2));
        rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void handleSlotSelection(int position) {
        TimeSlotModel clickedSlot = timeSlots.get(position);

        // Can't select if not available
        if (!clickedSlot.isAvailable()) {
            Toast.makeText(this, "This time slot is already booked", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clickedSlot.isSelected()) {
            // Deselect the slot
            clickedSlot.setSelected(false);
            selectedSlots.remove(clickedSlot);
            currentSelectedDuration--;
        } else {
            // Select the slot (if we haven't reached max duration)
            if (currentSelectedDuration < maxDuration) {
                clickedSlot.setSelected(true);
                selectedSlots.add(clickedSlot);
                currentSelectedDuration++;
            } else {
                Toast.makeText(this, "Maximum " + maxDuration + " hour(s) allowed", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Sort selected slots by time
        selectedSlots.sort((slot1, slot2) -> slot1.getStartTime().compareTo(slot2.getStartTime()));

        // Check if selected slots are consecutive (only if more than 1 hour selected)
        if (currentSelectedDuration > 1 && !areSlotsConsecutive(selectedSlots)) {
            Toast.makeText(this, "Please select consecutive time slots", Toast.LENGTH_SHORT).show();
            // Undo the last selection
            clickedSlot.setSelected(false);
            selectedSlots.remove(clickedSlot);
            currentSelectedDuration--;
            timeSlotAdapter.notifyDataSetChanged();
            return;
        }

        timeSlotAdapter.updateSelectedDuration(currentSelectedDuration);
        updateDurationText();
        btnConfirmBooking.setEnabled(currentSelectedDuration > 0);
        timeSlotAdapter.notifyDataSetChanged();
    }

    private boolean areSlotsConsecutive(List<TimeSlotModel> slots) {
        if (slots.size() <= 1) return true;

        for (int i = 0; i < slots.size() - 1; i++) {
            TimeSlotModel current = slots.get(i);
            TimeSlotModel next = slots.get(i + 1);

            if (!current.getEndTime().equals(next.getStartTime())) {
                return false;
            }
        }
        return true;
    }

    private void resetTimeSlotSelection() {
        for (TimeSlotModel slot : selectedSlots) {
            slot.setSelected(false);
        }
        selectedSlots.clear();
        currentSelectedDuration = 0;
        timeSlotAdapter.updateSelectedDuration(0);
        updateDurationText();
        btnConfirmBooking.setEnabled(false);
        timeSlotAdapter.notifyDataSetChanged();
    }

    private void updateDurationText() {
        String durationText = "Selected: " + currentSelectedDuration + "/" + maxDuration + " hour" +
                (maxDuration > 1 ? "s" : "");
        tvSelectedDuration.setText(durationText);
    }

    private void setupButton() {
        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadTimeSlots() {
        Log.d(TAG, "Loading time slots for: " + selectedDate);
        generateTimeSlots();
        fetchBookedSlots();
    }

    private void generateTimeSlots() {
        timeSlots.clear();
        selectedSlots.clear();
        currentSelectedDuration = 0;
        updateDurationText();

        try {
            int startHour = Integer.parseInt(availableFrom.split(":")[0]);
            int endHour = Integer.parseInt(availableTo.split(":")[0]);

            for (int hour = startHour; hour < endHour; hour++) {
                String startTime = String.format(Locale.getDefault(), "%02d:00", hour);
                String endTime = String.format(Locale.getDefault(), "%02d:00", hour + 1);
                String timeRange = startTime + " - " + endTime;
                TimeSlotModel slot = new TimeSlotModel(timeRange, startTime, endTime, true);
                timeSlots.add(slot);
            }
            timeSlotAdapter.notifyDataSetChanged();
            Log.d(TAG, "Generated " + timeSlots.size() + " time slots");
        } catch (Exception e) {
            Log.e(TAG, "Error generating time slots: " + e.getMessage());
            Toast.makeText(this, "Error generating time slots", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchBookedSlots() {
        db.collection("bookings")
                .whereEqualTo("service_type", serviceType)
                .whereEqualTo("location_id", locationId)
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " existing bookings");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String bookedStartTime = document.getString("start_time");
                        String bookedEndTime = document.getString("end_time");
                        markSlotsAsBooked(bookedStartTime, bookedEndTime);
                    }
                    timeSlotAdapter.notifyDataSetChanged();
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
        return (slotStart.compareTo(bookedEnd) < 0 && slotEnd.compareTo(bookedStart) > 0);
    }

    private void confirmBooking() {
        if (selectedSlots.isEmpty()) {
            Toast.makeText(this, R.string.please_select_time_slot, Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlotModel firstSlot = selectedSlots.get(0);
        TimeSlotModel lastSlot = selectedSlots.get(selectedSlots.size() - 1);

        Intent intent = new Intent(TimeSlotActivity.this, ConfirmBookingActivity.class);
        intent.putExtra("serviceType", serviceType);
        intent.putExtra("serviceName", serviceName);
        intent.putExtra("locationId", locationId);
        intent.putExtra("extraInfo", extraInfo);
        intent.putExtra("date", selectedDate);
        intent.putExtra("startTime", firstSlot.getStartTime());
        intent.putExtra("endTime", lastSlot.getEndTime());
        intent.putExtra("duration", currentSelectedDuration);
        intent.putExtra("isPaid", isPaid);
        intent.putExtra("price", price * currentSelectedDuration);
        startActivity(intent);
    }
}