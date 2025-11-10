package com.example.smartqueue;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText etAdminEmail, etAdminPassword;
    private Button btnAdminLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();

        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnAdminLogin.setOnClickListener(v -> attemptAdminLogin());
    }

    private void attemptAdminLogin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Check if user is admin in Firestore
                        checkAdminRole(mAuth.getCurrentUser().getUid());
                    } else {
                        Toast.makeText(AdminLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAdminRole(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String role = task.getResult().getString("role");
                        if ("admin".equals(role)) {
                            startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(AdminLoginActivity.this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    }
                });
    }
}