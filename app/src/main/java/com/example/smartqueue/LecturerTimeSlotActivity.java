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

public class LecturerTimeSlotActivity extends AppCompatActivity {

    private static final String TAG = "LecturerTimeSlot";
    private static final int MAX_BOOKING_DAYS = 7;

    private FirebaseFirestore db;
    private LecturerModel lecturer;
    private String lecturerId;

    private TextView tvLecturerName, tvDepartment, tvOfficeLocation, tvSelectedDate, tvSelectedDuration;
    private RecyclerView rvDates, rvTimeSlots;
    private Button btnConfirmBooking;
    private ImageView btnBack;

    private List<DateItemModel> dateList = new ArrayList<>();
    private List<TimeSlotModel> timeSlots = new ArrayList<>();
    private DateAdapter dateAdapter;
    private TimeSlotAdapter timeSlotAdapter;
    private String selectedDate;
    private String selectedDayOfWeek; // e.g., "monday"
    private TimeSlotModel selectedSlot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_time_slot_activity);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupTimeSlotRecyclerView(); // Initialize adapter FIRST
        getLecturerData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvLecturerName = findViewById(R.id.tvLecturerName);
        tvDepartment = findViewById(R.id.tvDepartment);
        tvOfficeLocation = findViewById(R.id.tvOfficeLocation);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedDuration = findViewById(R.id.tvSelectedDuration);
        rvDates = findViewById(R.id.rvDates);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Initialize timeSlots list
        timeSlots = new ArrayList<>();
    }

    private void setupTimeSlotRecyclerView() {
        timeSlotAdapter = new TimeSlotAdapter(timeSlots, new TimeSlotAdapter.OnTimeSlotClickListener() {
            @Override
            public void onTimeSlotClick(int position) {
                handleSlotSelection(position);
            }

            @Override
            public void onSelectionChanged(int selectedDuration, int maxDuration) {
                // Not used for lecturer consultation
            }
        }, 1); // Max 1 hour for consultation

        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 2));
        rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void getLecturerData() {
        Intent intent = getIntent();
        lecturerId = intent.getStringExtra("lecturerId");

        if (lecturerId == null) {
            Toast.makeText(this, "Error: No lecturer selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch full lecturer details from Firestore
        db.collection("lecturers").document(lecturerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        lecturer = documentSnapshot.toObject(LecturerModel.class);
                        if (lecturer != null) {
                            lecturer.setId(documentSnapshot.getId());
                            displayLecturerInfo();
                            setupDateList();
                            setupDateRecyclerView();
                            setupButtons();
                            // setupTimeSlotRecyclerView() removed from here - already called in onCreate
                        }
                    } else {
                        Toast.makeText(this, "Lecturer not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading lecturer: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayLecturerInfo() {
        tvLecturerName.setText(lecturer.getName());
        tvDepartment.setText(lecturer.getDepartment());
        tvOfficeLocation.setText(lecturer.getOffice_location());
        tvSelectedDuration.setText("Select 1 consultation slot (1 hour)");
    }

    private void setupDateList() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dayOfWeekShortFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        List<String> lecturerDays = lecturer.getAvailableDays();

        for (int i = 0; i < MAX_BOOKING_DAYS; i++) {
            String date = dateFormat.format(calendar.getTime());
            String fullDayName = dayOfWeekFormat.format(calendar.getTime()).toLowerCase();
            String dayOfWeek = dayOfWeekShortFormat.format(calendar.getTime()).toUpperCase();
            String dayOfMonth = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String monthYear = monthYearFormat.format(calendar.getTime());
            boolean isToday = (i == 0);

            // Only add dates where lecturer has consultation hours
            if (lecturerDays.contains(fullDayName)) {
                DateItemModel dateItem = new DateItemModel(date, dayOfWeek, dayOfMonth, monthYear, isToday);

                // Select first available day by default
                if (dateList.isEmpty()) {
                    dateItem.setSelected(true);
                    selectedDate = date;
                    selectedDayOfWeek = fullDayName;
                    updateSelectedDateDisplay(date);
                }

                dateList.add(dateItem);
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (dateList.isEmpty()) {
            Toast.makeText(this, "No available dates in the next 7 days", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDateRecyclerView() {
        dateAdapter = new DateAdapter(dateList, (date, position) -> {
            selectedDate = date;

            // Calculate day of week for selected date
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                selectedDayOfWeek = dayFormat.format(sdf.parse(date)).toLowerCase();
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date: " + e.getMessage());
            }

            updateSelectedDateDisplay(date);
            selectedSlot = null;
            loadTimeSlots();
            btnConfirmBooking.setEnabled(false);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvDates.setLayoutManager(layoutManager);
        rvDates.setAdapter(dateAdapter);

        // Load initial time slots
        if (!dateList.isEmpty()) {
            loadTimeSlots();
        }
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

    private void handleSlotSelection(int position) {
        TimeSlotModel clickedSlot = timeSlots.get(position);

        if (!clickedSlot.isAvailable()) {
            Toast.makeText(this, "This time slot is already booked", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deselect previous slot
        if (selectedSlot != null) {
            selectedSlot.setSelected(false);
        }

        // Select new slot
        clickedSlot.setSelected(true);
        selectedSlot = clickedSlot;

        if (timeSlotAdapter != null) {
            timeSlotAdapter.notifyDataSetChanged();
        }
        btnConfirmBooking.setEnabled(true);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void loadTimeSlots() {
        if (lecturer == null || selectedDayOfWeek == null) {
            Log.e(TAG, "Cannot load time slots: lecturer or selectedDayOfWeek is null");
            return;
        }

        Log.d(TAG, "Loading time slots for: " + selectedDate + " (" + selectedDayOfWeek + ")");
        generateTimeSlotsFromSchedule();
        fetchBookedSlots();
    }

    private void generateTimeSlotsFromSchedule() {
        if (timeSlots == null) {
            timeSlots = new ArrayList<>();
        } else {
            timeSlots.clear();
        }

        // Get slots for the selected day from lecturer's schedule
        List<String> daySlots = lecturer.getSlotsForDay(selectedDayOfWeek);

        if (daySlots == null || daySlots.isEmpty()) {
            Log.d(TAG, "No slots available for " + selectedDayOfWeek);
            if (timeSlotAdapter != null) {
                timeSlotAdapter.notifyDataSetChanged();
            }
            return;
        }

        // Parse each slot (format: "HH:mm-HH:mm")
        for (String slot : daySlots) {
            try {
                String[] times = slot.split("-");
                if (times.length == 2) {
                    String startTime = times[0].trim();
                    String endTime = times[1].trim();
                    String timeRange = startTime + " - " + endTime;

                    TimeSlotModel timeSlot = new TimeSlotModel(timeRange, startTime, endTime, true);
                    timeSlots.add(timeSlot);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing slot: " + slot, e);
            }
        }

        // SAFE CHECK: Make sure adapter is not null
        if (timeSlotAdapter != null) {
            timeSlotAdapter.notifyDataSetChanged();
            Log.d(TAG, "Generated " + timeSlots.size() + " time slots from schedule");
        } else {
            Log.e(TAG, "timeSlotAdapter is null in generateTimeSlotsFromSchedule");
        }
    }

    private void fetchBookedSlots() {
        if (lecturerId == null || selectedDate == null) {
            Log.e(TAG, "Cannot fetch booked slots: lecturerId or selectedDate is null");
            return;
        }

        db.collection("bookings")
                .whereEqualTo("service_type", "lecturer_consultation")
                .whereEqualTo("lecturer_id", lecturerId)
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
                    if (timeSlotAdapter != null) {
                        timeSlotAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching bookings: " + e.getMessage());
                    Toast.makeText(this, "Error loading availability", Toast.LENGTH_SHORT).show();
                });
    }

    private void markSlotsAsBooked(String bookedStart, String bookedEnd) {
        if (timeSlots == null) return;

        for (TimeSlotModel slot : timeSlots) {
            if (isTimeMatch(slot.getStartTime(), slot.getEndTime(), bookedStart, bookedEnd)) {
                slot.setAvailable(false);
                Log.d(TAG, "Marked slot as booked: " + slot.getTimeRange());
            }
        }
    }

    private boolean isTimeMatch(String slotStart, String slotEnd, String bookedStart, String bookedEnd) {
        // Exact match or overlap
        return (slotStart.equals(bookedStart) && slotEnd.equals(bookedEnd)) ||
                (slotStart.compareTo(bookedEnd) < 0 && slotEnd.compareTo(bookedStart) > 0);
    }

    private void confirmBooking() {
        if (selectedSlot == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ConfirmBookingActivity.class);
        intent.putExtra("serviceType", "lecturer_consultation");
        intent.putExtra("serviceName", "Consultation with " + lecturer.getName());
        intent.putExtra("locationId", lecturer.getOffice_location());
        intent.putExtra("lecturerId", lecturerId);
        intent.putExtra("lecturerName", lecturer.getName());
        intent.putExtra("lecturerEmail", lecturer.getEmail());
        intent.putExtra("date", selectedDate);
        intent.putExtra("startTime", selectedSlot.getStartTime());
        intent.putExtra("endTime", selectedSlot.getEndTime());
        intent.putExtra("duration", 1);
        intent.putExtra("isPaid", false);
        intent.putExtra("price", 0.0);
        startActivity(intent);
    }
}