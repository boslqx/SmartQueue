package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    private static final long AUTO_SCROLL_DELAY = 5000; // 5 seconds

    private TextView tvWelcomeUser, tvActiveBookings, tvUpcomingBookings, tvSeeAll;
    private TextView tvTotalBookingsCount, tvThisWeekCount;
    private ViewPager2 vpAnnouncements;
    private TabLayout tabIndicator;
    private RecyclerView recyclerRecent;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private AnnouncementAdapter announcementAdapter;
    private List<AnnouncementModel> announcementList;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;

    private RecentAdapter recentAdapter;
    private List<BookingModel> recentBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupAnnouncementCarousel();
        setupRecyclerView();
        setupClickListeners();

        loadUserData();
        loadAnnouncements(); // This should work now
        loadUserBookings();
        loadBookingStats();
    }

    private void initializeViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvActiveBookings = findViewById(R.id.tvActiveBookings);
        tvUpcomingBookings = findViewById(R.id.tvUpcomingBookings);
        tvSeeAll = findViewById(R.id.tvSeeAll);
        tvTotalBookingsCount = findViewById(R.id.tvTotalBookingsCount);
        tvThisWeekCount = findViewById(R.id.tvThisWeekCount);

        vpAnnouncements = findViewById(R.id.vpAnnouncements);
        tabIndicator = findViewById(R.id.tabIndicator);
        recyclerRecent = findViewById(R.id.recyclerRecent);
    }

    private void setupAnnouncementCarousel() {
        announcementList = new ArrayList<>();
        announcementAdapter = new AnnouncementAdapter(announcementList);
        vpAnnouncements.setAdapter(announcementAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabIndicator, vpAnnouncements,
                (tab, position) -> {
                    // Empty - just creates indicator dots
                }).attach();

        // Setup auto-scroll
        autoScrollHandler = new Handler(Looper.getMainLooper());
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (announcementList != null && announcementList.size() > 1) {
                    int currentItem = vpAnnouncements.getCurrentItem();
                    int nextItem = (currentItem + 1) % announcementList.size();
                    vpAnnouncements.setCurrentItem(nextItem, true);
                }
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        };
    }

    private void startAutoScroll() {
        stopAutoScroll(); // Stop any existing scroll
        if (announcementList != null && announcementList.size() > 1) {
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
            Log.d(TAG, "Auto-scroll started for " + announcementList.size() + " announcements");
        }
    }

    private void stopAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    private void setupRecyclerView() {
        recentBookings = new ArrayList<>();
        recentAdapter = new RecentAdapter(recentBookings, booking -> {
            Intent intent = new Intent(this, BookingDetailsActivity.class);
            intent.putExtra("bookingId", booking.getDocumentId());
            startActivity(intent);
        });

        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecent.setAdapter(recentAdapter);
    }

    private void setupClickListeners() {
        tvSeeAll.setOnClickListener(v ->
                startActivity(new Intent(this, QueueStatusActivity.class)));

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_book) {
                startActivity(new Intent(this, BookActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) {
            tvWelcomeUser.setText("Welcome, Guest!");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        if (name != null && !name.isEmpty()) {
                            String firstName = name.split(" ")[0];
                            tvWelcomeUser.setText("Welcome back, " + firstName + "!");
                        } else {
                            tvWelcomeUser.setText("Welcome back!");
                        }
                    } else {
                        tvWelcomeUser.setText("Welcome!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                    tvWelcomeUser.setText("Welcome!");
                });
    }

    private void loadAnnouncements() {
        Log.d(TAG, "Starting to load announcements...");

        db.collection("announcements")
                .whereEqualTo("active", true)
                .orderBy("priority", Query.Direction.DESCENDING)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Announcements query successful. Documents found: " + queryDocumentSnapshots.size());

                    announcementList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        AnnouncementModel announcement = document.toObject(AnnouncementModel.class);
                        announcement.setId(document.getId());

                        Log.d(TAG, "Loaded announcement: " + announcement.getTitle() + " - " + announcement.getMessage());
                        announcementList.add(announcement);
                    }

                    // If no announcements, add default one
                    if (announcementList.isEmpty()) {
                        Log.d(TAG, "No announcements found, adding default");
                        AnnouncementModel defaultAnnouncement = new AnnouncementModel();
                        defaultAnnouncement.setTitle("Welcome to SmartQueue!");
                        defaultAnnouncement.setMessage("Book your facilities easily and skip the wait.");
                        defaultAnnouncement.setType("info");
                        announcementList.add(defaultAnnouncement);
                    }

                    Log.d(TAG, "Total announcements to display: " + announcementList.size());

                    // Notify adapter and start auto-scroll
                    announcementAdapter.notifyDataSetChanged();
                    startAutoScroll();

                    Log.d(TAG, "Announcements loaded successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading announcements: " + e.getMessage(), e);

                    // Add default announcement on error
                    announcementList.clear();
                    AnnouncementModel defaultAnnouncement = new AnnouncementModel();
                    defaultAnnouncement.setTitle("Welcome!");
                    defaultAnnouncement.setMessage("Start booking your facilities today.");
                    defaultAnnouncement.setType("info");
                    announcementList.add(defaultAnnouncement);

                    announcementAdapter.notifyDataSetChanged();

                    Log.d(TAG, "Added default announcement due to error");
                });
    }

    private void loadUserBookings() {
        if (mAuth.getCurrentUser() == null) {
            tvSeeAll.setVisibility(View.GONE);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("bookings")
                .whereEqualTo("user_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recentBookings.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingModel booking = new BookingModel();
                        booking.setDocumentId(document.getId());
                        booking.setUserId(document.getString("user_id"));
                        booking.setServiceType(document.getString("service_type"));
                        booking.setServiceName(document.getString("service_name"));
                        booking.setLocationId(document.getString("location_id"));
                        booking.setDate(document.getString("date"));
                        booking.setStartTime(document.getString("start_time"));
                        booking.setEndTime(document.getString("end_time"));
                        booking.setDuration(document.getLong("duration") != null ?
                                document.getLong("duration").intValue() : 1);
                        booking.setAmount(document.getDouble("amount") != null ?
                                document.getDouble("amount") : 0.0);
                        booking.setPaymentStatus(document.getString("payment_status"));
                        booking.setStatus(document.getString("status"));
                        booking.setCreatedAt(document.getTimestamp("created_at"));
                        booking.setUpdatedAt(document.getTimestamp("updated_at"));

                        recentBookings.add(booking);
                    }

                    recentAdapter.notifyDataSetChanged();

                    if (recentBookings.isEmpty()) {
                        tvSeeAll.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading bookings: " + e.getMessage());
                });
    }

    private void loadBookingStats() {
        if (mAuth.getCurrentUser() == null) {
            tvActiveBookings.setText("0 active");
            tvUpcomingBookings.setText("0 upcoming");
            tvTotalBookingsCount.setText("0");
            tvThisWeekCount.setText("0");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());

        // Total bookings
        db.collection("bookings")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvTotalBookingsCount.setText(String.valueOf(count));
                });

        // Count active bookings (confirmed status)
        db.collection("bookings")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvActiveBookings.setText(count + (count == 1 ? " active booking" : " active bookings"));
                });

        // Count upcoming bookings (future dates)
        db.collection("bookings")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("status", "confirmed")
                .whereGreaterThanOrEqualTo("date", today)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvUpcomingBookings.setText(count + (count == 1 ? " upcoming" : " upcoming"));
                });

        // This week bookings
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        String weekStart = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 7);
        String weekEnd = dateFormat.format(calendar.getTime());

        db.collection("bookings")
                .whereEqualTo("user_id", userId)
                .whereGreaterThanOrEqualTo("date", weekStart)
                .whereLessThan("date", weekEnd)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvThisWeekCount.setText(String.valueOf(count));
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        loadUserData();
        loadUserBookings();
        loadBookingStats();
        startAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        stopAutoScroll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        stopAutoScroll();
    }
}