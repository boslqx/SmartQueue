package com.example.smartqueue;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // Add this import
import com.google.firebase.firestore.DocumentSnapshot; // Add this import

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvForgotPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Add Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Login button click
        btnLogin.setOnClickListener(v -> loginUser());

        // Go to register
        tvSignup.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterStep1Activity.class);
            startActivity(i);
        });

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(i);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter your password");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Check user role from Firestore
                            checkUserRole(user);
                        } else {
                            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(FirebaseUser user) {
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            String email = document.getString("email");

                            // EXTENSIVE DEBUGGING
                            Log.d("ROLE_DEBUG", "=== ROLE ANALYSIS ===");
                            Log.d("ROLE_DEBUG", "User Email: " + email);
                            Log.d("ROLE_DEBUG", "Raw Role from Firestore: '" + role + "'");
                            Log.d("ROLE_DEBUG", "Role length: " + (role != null ? role.length() : "null"));

                            // Test different comparison methods
                            boolean equalsAdmin = "admin".equals(role);
                            boolean equalsIgnoreCase = "admin".equalsIgnoreCase(role);
                            boolean trimAndEquals = role != null && "admin".equals(role.trim());

                            Log.d("ROLE_DEBUG", "Method 1 - equals('admin'): " + equalsAdmin);
                            Log.d("ROLE_DEBUG", "Method 2 - equalsIgnoreCase('admin'): " + equalsIgnoreCase);
                            Log.d("ROLE_DEBUG", "Method 3 - trim().equals('admin'): " + trimAndEquals);

                            // Determine final decision
                            boolean isAdmin = (role != null && "admin".equalsIgnoreCase(role.trim()));

                            Log.d("ROLE_DEBUG", "FINAL DECISION - Is Admin: " + isAdmin);

                            // Show comprehensive toast
                            String debugMsg = "Role: '" + role + "'" +
                                    "\nMethod1: " + equalsAdmin +
                                    "\nMethod2: " + equalsIgnoreCase +
                                    "\nMethod3: " + trimAndEquals +
                                    "\nFinal: " + (isAdmin ? "ADMIN" : "USER");
                            Toast.makeText(LoginActivity.this, debugMsg, Toast.LENGTH_LONG).show();

                            // Wait a moment to read the toast, then redirect
                            new android.os.Handler().postDelayed(() -> {
                                if (isAdmin) {
                                    Log.d("ROLE_DEBUG", "LAUNCHING ADMIN DASHBOARD");
                                    Toast.makeText(LoginActivity.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                } else {
                                    Log.d("ROLE_DEBUG", "LAUNCHING USER DASHBOARD");
                                    Toast.makeText(LoginActivity.this, "Welcome User!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                }
                                finish();
                            }, 2000); // 2 second delay to read debug info

                        } else {
                            Toast.makeText(LoginActivity.this, "No user document found in Firestore", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Firestore error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    }
                });
    }
}