package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendResetLink, btnBackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnSendResetLink.setOnClickListener(v -> sendPasswordResetEmail());
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Disable button and show loading state
        btnSendResetLink.setEnabled(false);
        btnSendResetLink.setText("Sending...");

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnSendResetLink.setEnabled(true);
                    btnSendResetLink.setText(R.string.send_reset_link);

                    if (task.isSuccessful()) {
                        // Show success dialog
                        showSuccessDialog(email);
                    } else {
                        // Show error message
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showSuccessDialog(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reset Link Sent! ✅")
                .setMessage("We've sent a password reset link to:\n\n" + email +
                        "\n\nPlease check your inbox (and spam folder) and click the link to reset your password.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Return to login screen
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}