package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterStep2Activity extends AppCompatActivity {

    Button btnSendCode, btnVerify;
    EditText etCode;
    TextView tvInstruction;

    String name, email, school, password;
    String generatedCode; // simulate a 6-digit code
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_step2_activity);

        mAuth = FirebaseAuth.getInstance();

        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerify = findViewById(R.id.btnVerify);
        etCode = findViewById(R.id.etCode);
        tvInstruction = findViewById(R.id.tvInstruction);

        Intent extra = getIntent();
        name = extra.getStringExtra("name");
        email = extra.getStringExtra("email");
        school = extra.getStringExtra("school");
        password = extra.getStringExtra("password");

        btnSendCode.setOnClickListener(v -> {
            // Generate a random code for immediate testing
            generatedCode = String.format("%06d", new Random().nextInt(999999));

            // Create user in Firebase Auth and send verification email
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Send Firebase verification email
                                user.sendEmailVerification()
                                        .addOnCompleteListener(sendTask -> {
                                            if (sendTask.isSuccessful()) {
                                                // Show both the actual Firebase email and the test code
                                                Toast.makeText(this,
                                                        "Verification email sent to " + email +
                                                                "\nTest Code: " + generatedCode,
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
                            Toast.makeText(this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnVerify.setOnClickListener(v -> {
            String entered = etCode.getText().toString().trim();

            // Option 1: Check with generated test code (for immediate testing)
            if (entered.equals(generatedCode)) {
                completeRegistration();
            }
            // Option 2: Check Firebase email verification status
            else {
                checkFirebaseEmailVerification();
            }
        });
    }

    private void checkFirebaseEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    completeRegistration();
                } else {
                    Toast.makeText(this,
                            "Email not verified yet. Please check your inbox or use the test code.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No user found. Please send verification code first.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void completeRegistration() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Save user data to Firestore
            saveUserToFirestore(user);
        } else {
            // Fallback: proceed without Firestore if user is null
            Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
            proceedToNextStep();
        }
    }

    private void saveUserToFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("school", school);
        userData.put("uid", user.getUid());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User data saved!", Toast.LENGTH_SHORT).show();
                    proceedToNextStep();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Still proceed to next step even if Firestore fails
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