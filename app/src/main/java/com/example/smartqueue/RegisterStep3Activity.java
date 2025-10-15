package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterStep3Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_step3_activity);

        Button btn = findViewById(R.id.btnContinue);
        btn.setOnClickListener(v -> {
            // user should now be considered logged in; in real app save auth state in SharedPreferences
            startActivity(new Intent(this, DashboardActivity.class));
            finishAffinity();
        });
    }
}

