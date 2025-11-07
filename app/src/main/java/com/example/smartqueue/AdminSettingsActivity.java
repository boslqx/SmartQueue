package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AdminSettings";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Views
    private ImageView ivProfilePicture, btnBack;
    private TextView tvUserName, tvUserEmail, tvAppVersion;
    private Button btnEditProfile, btnChangePassword, btnManageAnnouncements;
    private Button btnManageServices, btnViewBookings, btnViewUsers, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FIXED: Changed to match the actual XML filename
        setContentView(R.layout.activity_admin_settings);

        initializeFirebase();
        initializeViews();
        loadUserInfo();
        setupClickListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);

        // Profile section
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Admin Management
        btnManageAnnouncements = findViewById(R.id.btnManageAnnouncements);
        btnManageServices = findViewById(R.id.btnManageServices);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnViewUsers = findViewById(R.id.btnViewUsers);

        // About
        tvAppVersion = findViewById(R.id.tvAppVersion);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Set email from Firebase Auth
            tvUserEmail.setText(user.getEmail());

            // Fetch additional user info from Firestore
            db.collection("admins").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String role = documentSnapshot.getString("role");

                            tvUserName.setText(name != null ? name : "Admin");

                            // You can display role if needed
                            if (role != null) {
                                tvUserEmail.append("\n" + role);
                            }
                        } else {
                            // Use display name from Auth if Firestore doc doesn't exist
                            String displayName = user.getDisplayName();
                            tvUserName.setText(displayName != null ? displayName : "Admin");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading admin info: " + e.getMessage());
                        String displayName = user.getDisplayName();
                        tvUserName.setText(displayName != null ? displayName : "Admin");
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

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEditProfileActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnManageAnnouncements.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnnouncementManagementActivity.class);
            startActivity(intent);
        });

        btnManageServices.setOnClickListener(v -> {
            // TODO: Implement Service Management
            Toast.makeText(this, "Service Management - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnViewBookings.setOnClickListener(v -> {
            // TODO: Implement View All Bookings
            Toast.makeText(this, "View All Bookings - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        btnViewUsers.setOnClickListener(v -> {
            // TODO: Implement User Management
            Toast.makeText(this, "User Management - Coming Soon", Toast.LENGTH_SHORT).show();
        });

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
    }
}