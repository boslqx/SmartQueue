package com.example.smartqueue;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminEditProfileActivity extends AppCompatActivity {

    private static final String TAG = "AdminEditProfile";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ImageView btnBack;
    private EditText etName, etEmail, etRole;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        loadCurrentUserInfo();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etRole = findViewById(R.id.etRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void loadCurrentUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            etEmail.setText(user.getEmail());
            etEmail.setEnabled(false); // Email cannot be changed easily

            // Load from Firestore
            db.collection("admins").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String role = documentSnapshot.getString("role");

                            etName.setText(name != null ? name : "");
                            etRole.setText(role != null ? role : "Administrator");
                        } else {
                            // Use display name from Auth if Firestore doc doesn't exist
                            String displayName = user.getDisplayName();
                            etName.setText(displayName != null ? displayName : "");
                            etRole.setText("Administrator");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading profile: " + e.getMessage());
                        // Fallback to Auth display name
                        String displayName = user.getDisplayName();
                        etName.setText(displayName != null ? displayName : "");
                        etRole.setText("Administrator");
                    });
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String role = etRole.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (role.isEmpty()) {
            etRole.setError("Role is required");
            etRole.requestFocus();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Update in Firestore
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("name", name);
            adminData.put("email", user.getEmail());
            adminData.put("role", role);
            adminData.put("uid", user.getUid());

            db.collection("admins").document(user.getUid())
                    .set(adminData)
                    .addOnSuccessListener(aVoid -> {
                        // Also update Firebase Auth display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Profile updated successfully",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating Auth profile: " + e.getMessage());
                                    Toast.makeText(this, "Profile updated with warning: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating profile: " + e.getMessage());
                        Toast.makeText(this, "Error updating profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText("Save Profile");
                    });
        }
    }
}