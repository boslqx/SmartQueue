package com.example.smartqueue;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditLecturerActivity extends AppCompatActivity {

    private static final String TAG = "AddEditLecturer";
    private FirebaseFirestore db;

    private ImageView btnBack;
    private TextView tvTitle;
    private EditText etName, etEmail, etDepartment, etOffice, etWeeklyHours;
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday;
    private LinearLayout llTimeSlots;
    private Button btnSave;

    private String lecturerId;
    private boolean isEditMode = false;

    // Time slot views holder
    private final List<TimeSlotRow> timeSlotRows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_lecturer_activity);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        checkEditMode();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etDepartment = findViewById(R.id.etDepartment);
        etOffice = findViewById(R.id.etOffice);
        etWeeklyHours = findViewById(R.id.etWeeklyHours);
        cbMonday = findViewById(R.id.cbMonday);
        cbTuesday = findViewById(R.id.cbTuesday);
        cbWednesday = findViewById(R.id.cbWednesday);
        cbThursday = findViewById(R.id.cbThursday);
        cbFriday = findViewById(R.id.cbFriday);
        llTimeSlots = findViewById(R.id.llTimeSlots);
        btnSave = findViewById(R.id.btnSave);

        // Add initial time slot row
        addTimeSlotRow();
    }

    private void checkEditMode() {
        lecturerId = getIntent().getStringExtra("lecturer_id");

        if (lecturerId != null) {
            isEditMode = true;
            tvTitle.setText("Edit Lecturer");
            btnSave.setText("Update Lecturer");

            // Populate fields
            etName.setText(getIntent().getStringExtra("lecturer_name"));
            etEmail.setText(getIntent().getStringExtra("lecturer_email"));
            etDepartment.setText(getIntent().getStringExtra("lecturer_department"));
            etOffice.setText(getIntent().getStringExtra("lecturer_office"));
            etWeeklyHours.setText(String.valueOf(getIntent().getIntExtra("lecturer_weekly_hours", 5)));

            // Load consultation schedule
            loadConsultationSchedule();
        } else {
            tvTitle.setText("Add Lecturer");
            btnSave.setText("Add Lecturer");
        }
    }

    private void loadConsultationSchedule() {
        db.collection("lecturers").document(lecturerId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Map<String, Object> schedule = (Map<String, Object>) document.get("consultation_schedule");
                        if (schedule != null) {
                            // Check days
                            cbMonday.setChecked(schedule.containsKey("monday"));
                            cbTuesday.setChecked(schedule.containsKey("tuesday"));
                            cbWednesday.setChecked(schedule.containsKey("wednesday"));
                            cbThursday.setChecked(schedule.containsKey("thursday"));
                            cbFriday.setChecked(schedule.containsKey("friday"));

                            // Load time slots (example from first day that has slots)
                            for (Map.Entry<String, Object> entry : schedule.entrySet()) {
                                List<String> slots = (List<String>) entry.getValue();
                                if (slots != null && !slots.isEmpty()) {
                                    // Clear default row
                                    llTimeSlots.removeAllViews();
                                    timeSlotRows.clear();

                                    // Add slots
                                    for (String slot : slots) {
                                        String[] times = slot.split("-");
                                        if (times.length == 2) {
                                            addTimeSlotRow(times[0].trim(), times[1].trim());
                                        }
                                    }
                                    break; // Only load from first day
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading schedule", e);
                });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnAddTimeSlot).setOnClickListener(v -> addTimeSlotRow());

        btnSave.setOnClickListener(v -> saveLecturer());
    }

    private void addTimeSlotRow() {
        addTimeSlotRow("", "");
    }

    private void addTimeSlotRow(String startTime, String endTime) {
        View row = getLayoutInflater().inflate(R.layout.item_time_slot_input, llTimeSlots, false);

        EditText etStart = row.findViewById(R.id.etStartTime);
        EditText etEnd = row.findViewById(R.id.etEndTime);
        ImageView btnRemove = row.findViewById(R.id.btnRemoveSlot);

        if (!startTime.isEmpty()) etStart.setText(startTime);
        if (!endTime.isEmpty()) etEnd.setText(endTime);

        TimeSlotRow timeSlotRow = new TimeSlotRow(row, etStart, etEnd);
        timeSlotRows.add(timeSlotRow);

        btnRemove.setOnClickListener(v -> {
            llTimeSlots.removeView(row);
            timeSlotRows.remove(timeSlotRow);
        });

        llTimeSlots.addView(row);
    }

    private void saveLecturer() {
        // Validate inputs
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String office = etOffice.getText().toString().trim();
        String weeklyHoursStr = etWeeklyHours.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || department.isEmpty() ||
                office.isEmpty() || weeklyHoursStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int weeklyHours;
        try {
            weeklyHours = Integer.parseInt(weeklyHoursStr);
            if (weeklyHours <= 0) {
                Toast.makeText(this, "Weekly hours must be positive", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weekly hours", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected days and build schedule
        Map<String, List<String>> consultationSchedule = new HashMap<>();
        List<String> timeSlots = collectTimeSlots();

        if (timeSlots.isEmpty()) {
            Toast.makeText(this, "Please add at least one time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cbMonday.isChecked()) consultationSchedule.put("monday", new ArrayList<>(timeSlots));
        if (cbTuesday.isChecked()) consultationSchedule.put("tuesday", new ArrayList<>(timeSlots));
        if (cbWednesday.isChecked()) consultationSchedule.put("wednesday", new ArrayList<>(timeSlots));
        if (cbThursday.isChecked()) consultationSchedule.put("thursday", new ArrayList<>(timeSlots));
        if (cbFriday.isChecked()) consultationSchedule.put("friday", new ArrayList<>(timeSlots));

        if (consultationSchedule.isEmpty()) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare lecturer data
        Map<String, Object> lecturerData = new HashMap<>();
        lecturerData.put("name", name);
        lecturerData.put("email", email);
        lecturerData.put("department", department);
        lecturerData.put("office_location", office);
        lecturerData.put("weekly_hours", weeklyHours);
        lecturerData.put("consultation_schedule", consultationSchedule);
        lecturerData.put("updated_at", Timestamp.now());

        if (!isEditMode) {
            lecturerData.put("created_at", Timestamp.now());
            lecturerData.put("booked_hours", 0);
        }

        btnSave.setEnabled(false);
        btnSave.setText(isEditMode ? "Updating..." : "Adding...");

        if (isEditMode) {
            updateLecturer(lecturerData);
        } else {
            addLecturer(lecturerData);
        }
    }

    private List<String> collectTimeSlots() {
        List<String> slots = new ArrayList<>();

        for (TimeSlotRow row : timeSlotRows) {
            String start = row.etStart.getText().toString().trim();
            String end = row.etEnd.getText().toString().trim();

            if (!start.isEmpty() && !end.isEmpty()) {
                slots.add(start + "-" + end);
            }
        }

        return slots;
    }

    private void addLecturer(Map<String, Object> lecturerData) {
        db.collection("lecturers")
                .add(lecturerData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Lecturer added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add lecturer: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Add Lecturer");
                    Log.e(TAG, "Error adding lecturer", e);
                });
    }

    private void updateLecturer(Map<String, Object> lecturerData) {
        db.collection("lecturers").document(lecturerId)
                .update(lecturerData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lecturer updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update lecturer: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Update Lecturer");
                    Log.e(TAG, "Error updating lecturer", e);
                });
    }

    private static class TimeSlotRow {
        View view;
        EditText etStart;
        EditText etEnd;

        TimeSlotRow(View view, EditText etStart, EditText etEnd) {
            this.view = view;
            this.etStart = etStart;
            this.etEnd = etEnd;
        }
    }
}