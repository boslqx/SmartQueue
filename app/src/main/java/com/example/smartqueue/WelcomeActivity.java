package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.welcome_activity);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterStep1Activity.class))
        );
        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(() -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null && user.isEmailVerified()) {
                Log.d(TAG, "User is logged in and verified: " + user.getEmail());
                checkUserRoleAndRedirect(user.getUid());
            } else {
                Log.d(TAG, "User not logged in or not verified");
            }
        }, 1000);
    }

    private void checkUserRoleAndRedirect(String userId) {
        Log.d(TAG, "Checking role for user ID: " + userId);

        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Log ALL fields for debugging
                            Log.d(TAG, "=== USER DOCUMENT DATA ===");
                            Log.d(TAG, "Email: " + document.getString("email"));
                            Log.d(TAG, "Name: " + document.getString("name"));
                            Log.d(TAG, "School: " + document.getString("school"));
                            Log.d(TAG, "UID: " + document.getString("uid"));

                            // Check for role field
                            String role = document.getString("role");
                            Log.d(TAG, "ROLE FIELD VALUE: " + role);

                            Intent intent;
                            if (role != null && role.equals("admin")) {
                                Log.d(TAG, "✓ ADMIN DETECTED - Redirecting to Admin Dashboard");
                                intent = new Intent(WelcomeActivity.this, AdminDashboardActivity.class);
                            } else {
                                Log.d(TAG, "✗ USER DETECTED - Redirecting to User Dashboard");
                                intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
                            }

                            startActivity(intent);
                            finish();

                        } else {
                            Log.d(TAG, "User document does not exist");
                            // Default to user dashboard
                            Intent intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Error getting user document: " + task.getException());
                        // Default to user dashboard on error
                        Intent intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}