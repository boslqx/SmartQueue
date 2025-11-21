package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterStep1Activity extends AppCompatActivity {

    // CONFIGURE EMAIL DOMAIN HERE
    private static final String ALLOWED_EMAIL_DOMAIN = "@student.newiniti.edu.my";

    EditText etName, etEmail, etPassword, etConfirm;
    Spinner spinnerSchool;
    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_step1_activity);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        btnNext = findViewById(R.id.btnNext);

        String[] schools = {"School of Computing", "School of Business", "School of Education"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schools);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchool.setAdapter(adapter);

        btnNext.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();
            String confirm = etConfirm.getText().toString();

            // Validate all fields
            if (TextUtils.isEmpty(name)) {
                etName.setError("Name is required");
                etName.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            // VALIDATE STUDENT EMAIL DOMAIN
            if (!isValidStudentEmail(email)) {
                etEmail.setError("Please use your student email (" + ALLOWED_EMAIL_DOMAIN + ")");
                etEmail.requestFocus();
                Toast.makeText(this, "Only student emails are allowed!", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(pass)) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            if (pass.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            if (!pass.equals(confirm)) {
                etConfirm.setError("Passwords do not match");
                etConfirm.requestFocus();
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // All validations passed - proceed to next step
            Intent i = new Intent(this, RegisterStep2Activity.class);
            i.putExtra("name", name);
            i.putExtra("email", email);
            i.putExtra("school", spinnerSchool.getSelectedItem().toString());
            i.putExtra("password", pass);
            startActivity(i);
        });
    }

    /**
     * Validates if the email ends with the allowed student domain
     * @param email The email address to validate
     * @return true if valid student email, false otherwise
     */
    private boolean isValidStudentEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        // Convert to lowercase for case-insensitive comparison
        String emailLower = email.toLowerCase();

        // Check if email ends with the allowed domain
        boolean endsWithDomain = emailLower.endsWith(ALLOWED_EMAIL_DOMAIN.toLowerCase());

        // Optional: Additional validation for email format
        // Check if there's at least one character before the @ symbol
        boolean hasUsername = emailLower.indexOf('@') > 0;

        return endsWithDomain && hasUsername;
    }
}