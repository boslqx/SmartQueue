package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is already logged in
        checkCurrentUser();
    }

    private void checkCurrentUser() {
        if (mAuth.getCurrentUser() != null) {
            // User is logged in, check their role
            checkUserRoleAndRedirect();
        } else {
            // No user logged in, go to login screen
            redirectToLogin();
        }
    }

    private void checkUserRoleAndRedirect() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        String role = task.getResult().getString("role");

                        if ("admin".equals(role)) {
                            // Redirect to admin dashboard
                            redirectToAdminDashboard();
                        } else {
                            // Redirect to regular user dashboard
                            redirectToUserDashboard();
                        }
                    } else {
                        // If can't get user data, go to login
                        redirectToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    // If there's an error, go to login
                    redirectToLogin();
                });
    }

    private void redirectToAdminDashboard() {
        startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
        finish();
    }

    private void redirectToUserDashboard() {
        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        finish();
    }

    private void redirectToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}