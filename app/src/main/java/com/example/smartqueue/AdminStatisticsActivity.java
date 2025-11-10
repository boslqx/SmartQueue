package com.example.smartqueue;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.*;

public class AdminStatisticsActivity extends AppCompatActivity {

    private static final String TAG = "AdminStatistics";
    private FirebaseFirestore db;

    // Views
    private ImageView btnBack, btnRefresh;
    private TextView tvTotalBookings, tvActiveBookings, tvTotalUsers, tvTotalLecturers;
    private TextView tvMonthlyBookings, tvMonthlyCompleted, tvMonthlyCancelled;
    private RecyclerView recyclerViewTopLecturers;
    private LinearLayout llPopularDays;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_statistics);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
        loadStatistics();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);

        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvActiveBookings = findViewById(R.id.tvActiveBookings);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalLecturers = findViewById(R.id.tvTotalLecturers);

        tvMonthlyBookings = findViewById(R.id.tvMonthlyBookings);
        tvMonthlyCompleted = findViewById(R.id.tvMonthlyCompleted);
        tvMonthlyCancelled = findViewById(R.id.tvMonthlyCancelled);

        recyclerViewTopLecturers = findViewById(R.id.recyclerViewTopLecturers);
        llPopularDays = findViewById(R.id.llPopularDays);
        progressBar = findViewById(R.id.progressBar);

        recyclerViewTopLecturers.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadStatistics());
    }

    private void loadStatistics() {
        progressBar.setVisibility(View.VISIBLE);

        // Load all statistics
        loadBookingStats();
        loadUserStats();
        loadLecturerStats();
        loadMonthlyStats();
        loadTopLecturers();
        loadPopularDays();
    }

    private void loadBookingStats() {
        db.collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    int active = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String status = document.getString("status");
                        if ("confirmed".equals(status)) {
                            active++;
                        }
                    }

                    tvTotalBookings.setText(String.valueOf(total));
                    tvActiveBookings.setText(String.valueOf(active));

                    Log.d(TAG, "Loaded booking stats: Total=" + total + ", Active=" + active);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading booking stats", e);
                    tvTotalBookings.setText("0");
                    tvActiveBookings.setText("0");
                });
    }

    private void loadUserStats() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = queryDocumentSnapshots.size();
                    tvTotalUsers.setText(String.valueOf(totalUsers));
                    Log.d(TAG, "Loaded user stats: " + totalUsers);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user stats", e);
                    tvTotalUsers.setText("0");
                });
    }

    private void loadLecturerStats() {
        db.collection("lecturers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalLecturers = queryDocumentSnapshots.size();
                    tvTotalLecturers.setText(String.valueOf(totalLecturers));
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "Loaded lecturer stats: " + totalLecturers);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading lecturer stats", e);
                    tvTotalLecturers.setText("0");
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void loadMonthlyStats() {
        // Get current month start
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date monthStart = calendar.getTime();

        db.collection("bookings")
                .whereGreaterThanOrEqualTo("created_at", monthStart)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    int completed = 0;
                    int cancelled = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String status = document.getString("status");
                        if ("completed".equals(status)) {
                            completed++;
                        } else if ("cancelled".equals(status)) {
                            cancelled++;
                        }
                    }

                    tvMonthlyBookings.setText(String.valueOf(total));
                    tvMonthlyCompleted.setText(String.valueOf(completed));
                    tvMonthlyCancelled.setText(String.valueOf(cancelled));

                    Log.d(TAG, "Monthly stats: Total=" + total + ", Completed=" + completed + ", Cancelled=" + cancelled);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading monthly stats", e);
                });
    }

    private void loadTopLecturers() {
        db.collection("lecturers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LecturerStat> lecturerStats = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String lecturerId = document.getId();
                        String name = document.getString("name");
                        Long bookedHours = document.getLong("booked_hours");

                        if (bookedHours != null && bookedHours > 0) {
                            lecturerStats.add(new LecturerStat(lecturerId, name, bookedHours.intValue()));
                        }
                    }

                    // Sort by booked hours
                    Collections.sort(lecturerStats, (a, b) -> Integer.compare(b.bookedHours, a.bookedHours));

                    // Get top 5
                    List<LecturerStat> topFive = lecturerStats.size() > 5 ?
                            lecturerStats.subList(0, 5) : lecturerStats;

                    displayTopLecturers(topFive);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading top lecturers", e);
                });
    }

    private void displayTopLecturers(List<LecturerStat> topLecturers) {
        if (topLecturers.isEmpty()) {
            return;
        }

        TopLecturerAdapter adapter = new TopLecturerAdapter(topLecturers);
        recyclerViewTopLecturers.setAdapter(adapter);
    }

    private void loadPopularDays() {
        db.collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> dayCount = new HashMap<>();
                    dayCount.put("Monday", 0);
                    dayCount.put("Tuesday", 0);
                    dayCount.put("Wednesday", 0);
                    dayCount.put("Thursday", 0);
                    dayCount.put("Friday", 0);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String day = document.getString("day");
                        if (day != null && dayCount.containsKey(day)) {
                            dayCount.put(day, dayCount.get(day) + 1);
                        }
                    }

                    displayPopularDays(dayCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading popular days", e);
                });
    }

    private void displayPopularDays(Map<String, Integer> dayCount) {
        llPopularDays.removeAllViews();

        // Sort by count
        List<Map.Entry<String, Integer>> sortedDays = new ArrayList<>(dayCount.entrySet());
        sortedDays.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Integer> entry : sortedDays) {
            View dayRow = getLayoutInflater().inflate(R.layout.item_popular_day, llPopularDays, false);

            TextView tvDay = dayRow.findViewById(R.id.tvDay);
            TextView tvCount = dayRow.findViewById(R.id.tvCount);

            tvDay.setText(entry.getKey());
            tvCount.setText(entry.getValue() + " bookings");

            llPopularDays.addView(dayRow);
        }
    }

    // Helper class for lecturer statistics
    private static class LecturerStat {
        String id;
        String name;
        int bookedHours;

        LecturerStat(String id, String name, int bookedHours) {
            this.id = id;
            this.name = name;
            this.bookedHours = bookedHours;
        }
    }

    // Adapter for top lecturers
    private static class TopLecturerAdapter extends RecyclerView.Adapter<TopLecturerAdapter.ViewHolder> {
        private final List<LecturerStat> lecturers;

        TopLecturerAdapter(List<LecturerStat> lecturers) {
            this.lecturers = lecturers;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_top_lecturer, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LecturerStat stat = lecturers.get(position);
            holder.tvRank.setText("#" + (position + 1));
            holder.tvName.setText(stat.name);
            holder.tvBookings.setText(stat.bookedHours + " hours");
        }

        @Override
        public int getItemCount() {
            return lecturers.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvBookings;

            ViewHolder(View itemView) {
                super(itemView);
                tvRank = itemView.findViewById(R.id.tvRank);
                tvName = itemView.findViewById(R.id.tvLecturerName);
                tvBookings = itemView.findViewById(R.id.tvBookings);
            }
        }
    }
}