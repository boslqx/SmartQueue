package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterStep2Activity extends AppCompatActivity {

    private Button btnResendEmail, btnVerify;
    private TextView tvUserEmail;

    private String name, email, school, password;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_step2_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnResendEmail = findViewById(R.id.btnResendEmail);
        btnVerify = findViewById(R.id.btnVerify);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        Intent extra = getIntent();
        name = extra.getStringExtra("name");
        email = extra.getStringExtra("email");
        school = extra.getStringExtra("school");
        password = extra.getStringExtra("password");

        // Display user's email
        tvUserEmail.setText(email);

        // Create account and send verification email automatically
        createAccountAndSendEmail();

        // Resend email button
        btnResendEmail.setOnClickListener(v -> resendVerificationEmail());

        // Verify button - check if user has verified their email
        btnVerify.setOnClickListener(v -> checkEmailVerification());
    }

    private void createAccountAndSendEmail() {
        // Show loading state
        btnResendEmail.setEnabled(false);
        btnVerify.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Send verification email
                            user.sendEmailVerification()
                                    .addOnCompleteListener(sendTask -> {
                                        btnResendEmail.setEnabled(true);
                                        btnVerify.setEnabled(true);

                                        if (sendTask.isSuccessful()) {
                                            Toast.makeText(this,
                                                    "Verification email sent! Please check your inbox.",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this,
                                                    "Failed to send verification email: " +
                                                            sendTask.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        btnResendEmail.setEnabled(true);
                        btnVerify.setEnabled(true);

                        // Account already exists or other error
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";

                        if (errorMsg.contains("already in use")) {
                            // Email already registered - just ask them to verify
                            Toast.makeText(this,
                                    "This email is already registered. Please verify it.",
                                    Toast.LENGTH_LONG).show();

                            // Try to sign in and send verification
                            signInAndResendEmail();
                        } else {
                            Toast.makeText(this, "Registration failed: " + errorMsg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInAndResendEmail() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && !user.isEmailVerified()) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(sendTask -> {
                                        if (sendTask.isSuccessful()) {
                                            Toast.makeText(this,
                                                    "Verification email resent!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            btnResendEmail.setEnabled(false);
            btnResendEmail.setText("Sending...");

            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        btnResendEmail.setEnabled(true);
                        btnResendEmail.setText(R.string.resend_verification_email);

                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Verification email sent! Check your inbox.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "Failed to send email: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please restart registration.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            btnVerify.setEnabled(false);
            btnVerify.setText("Checking...");

            // Reload user to get latest verification status
            user.reload().addOnCompleteListener(task -> {
                btnVerify.setEnabled(true);
                btnVerify.setText(R.string.i_verified_continue);

                if (user.isEmailVerified()) {
                    // Email is verified! Save user data and proceed
                    saveUserToFirestore(user);
                } else {
                    Toast.makeText(this,
                            "Email not verified yet. Please check your inbox and click the verification link.",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "No user found. Please restart registration.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserToFirestore(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("school", school);
        userData.put("uid", user.getUid());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Email verified successfully!",
                            Toast.LENGTH_SHORT).show();
                    proceedToNextStep();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Still proceed even if Firestore fails
                    proceedToNextStep();
                });
    }

    private void proceedToNextStep() {
        Intent i = new Intent(this, RegisterStep3Activity.class);
        i.putExtra("name", name);
        i.putExtra("email", email);
        i.putExtra("school", school);
        startActivity(i);
        finish();
    }
}