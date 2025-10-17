package com.example.smartqueue;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterStep1Activity extends AppCompatActivity {

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

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, RegisterStep2Activity.class);
            i.putExtra("name", name);
            i.putExtra("email", email);
            i.putExtra("school", spinnerSchool.getSelectedItem().toString());
            i.putExtra("password", pass);
            startActivity(i);
        });
    }
}
