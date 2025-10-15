package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class RegisterStep2Activity extends AppCompatActivity {

    Button btnSendCode, btnVerify;
    EditText etCode;
    TextView tvInstruction;

    String name, email, school, password;
    String generatedCode; // simulate a 6-digit code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_step2_activity);

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
            generatedCode = String.format("%06d", new Random().nextInt(999999));
            Toast.makeText(this, "Verification code sent to " + email + "\n(Code: " + generatedCode + ")", Toast.LENGTH_LONG).show();
            // In production, you'd send the code via email using backend or Firebase
        });

        btnVerify.setOnClickListener(v -> {
            String entered = etCode.getText().toString().trim();
            if (entered.equals(generatedCode)) {
                Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, RegisterStep3Activity.class);
                i.putExtra("name", name);
                i.putExtra("email", email);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
