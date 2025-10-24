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

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ImageView btnBack;
    private EditText etName, etEmail;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

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
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void loadCurrentUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            etEmail.setText(user.getEmail());
            etEmail.setEnabled(false); // Email cannot be changed easily

            // Load from Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String school = documentSnapshot.getString("school");

                            etName.setText(name != null ? name : "");
                        } else {
                            // Use display name from Auth if Firestore doc doesn't exist
                            String displayName = user.getDisplayName();
                            etName.setText(displayName != null ? displayName : "");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading profile: " + e.getMessage());
                        // Fallback to Auth display name
                        String displayName = user.getDisplayName();
                        etName.setText(displayName != null ? displayName : "");
                    });
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Update in Firestore
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", user.getEmail());
            userData.put("uid", user.getUid());

            db.collection("users").document(user.getUid())
                    .set(userData)
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