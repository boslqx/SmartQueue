package com.example.smartqueue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "SmartQueuePrefs";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    // Views
    private ImageView btnBack, ivProfilePicture;
    private TextView tvUserName, tvUserEmail, tvAppVersion;
    private Button btnEditProfile, btnChangePassword, btnFAQ, btnContactSupport, btnLogout;
    private SwitchMaterial switchBookingReminders, switchQueueUpdates, switchSoundNotifications, switchDarkMode;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        initializeFirebase();
        initializeViews();
        loadUserInfo();
        loadPreferences();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void initializeViews() {
        // Top bar
        btnBack = findViewById(R.id.btnBack);

        // Profile section
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Notifications
        switchBookingReminders = findViewById(R.id.switchBookingReminders);
        switchQueueUpdates = findViewById(R.id.switchQueueUpdates);
        switchSoundNotifications = findViewById(R.id.switchSoundNotifications);

        // Preferences
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Help & Support
        btnFAQ = findViewById(R.id.btnFAQ);
        btnContactSupport = findViewById(R.id.btnContactSupport);

        // About
        tvAppVersion = findViewById(R.id.tvAppVersion);
        btnLogout = findViewById(R.id.btnLogout);

        // Bottom Navigation
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_book) {
                startActivity(new Intent(this, BookActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                // Already here
                return true;
            }
            return false;
        });

        // Set the current selected item
        bottomNav.setSelectedItemId(R.id.nav_settings);
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Set email from Firebase Auth
            tvUserEmail.setText(user.getEmail());

            // Fetch additional user info from Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String school = documentSnapshot.getString("school");

                            tvUserName.setText(name != null ? name : "User");
                        } else {
                            // Use display name from Auth if Firestore doc doesn't exist
                            String displayName = user.getDisplayName();
                            tvUserName.setText(displayName != null ? displayName : "User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user info: " + e.getMessage());
                        String displayName = user.getDisplayName();
                        tvUserName.setText(displayName != null ? displayName : "User");
                    });
        } else {
            tvUserName.setText("Guest");
            tvUserEmail.setText("Not logged in");
        }

        // Set app version
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText("Version " + versionName);
        } catch (Exception e) {
            tvAppVersion.setText("Version 1.0.0");
        }
    }

    private void loadPreferences() {
        // Load notification preferences
        switchBookingReminders.setChecked(prefs.getBoolean("booking_reminders", true));
        switchQueueUpdates.setChecked(prefs.getBoolean("queue_updates", true));
        switchSoundNotifications.setChecked(prefs.getBoolean("sound_notifications", true));

        // Load dark mode preference
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Notification switches
        switchBookingReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("booking_reminders", isChecked).apply();
            Toast.makeText(this, "Booking reminders " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        switchQueueUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("queue_updates", isChecked).apply();
            Toast.makeText(this, "Queue updates " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        switchSoundNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sound_notifications", isChecked).apply();
            Toast.makeText(this, "Sound notifications " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        // Dark mode switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            Toast.makeText(this, "Dark mode " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        // FAQ
        btnFAQ.setOnClickListener(v -> {
            Intent intent = new Intent(this, FAQActivity.class);
            startActivity(intent);
        });

        // Contact Support
        btnContactSupport.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", "support@smartqueue.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SmartQueue App Support");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Describe your issue here...");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email client installed", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        // Sign out from Firebase
        mAuth.signOut();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user info when returning from edit profile
        loadUserInfo();
        // Ensure bottom navigation is properly selected
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_settings);
        }
    }
}