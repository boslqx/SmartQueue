package com.example.smartqueue;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Minimal programmatic layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(100, 100, 100, 100);

        TextView title = new TextView(this);
        title.setText("SERVICE MANAGEMENT");
        title.setTextSize(20);

        TextView message = new TextView(this);
        message.setText("Service management page - working!");
        message.setTextSize(16);
        message.setPadding(0, 50, 0, 0);

        layout.addView(title);
        layout.addView(message);

        setContentView(layout);

        Toast.makeText(this, "Service Management Loaded!", Toast.LENGTH_LONG).show();
    }
}