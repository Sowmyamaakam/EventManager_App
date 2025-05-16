package com.example.eventlogin1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddAdmin extends AppCompatActivity {

    private EditText newAdminEmail, newAdminPassword;
    private Button addAdminButton;
    private Set<String> adminEmails;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addadmin);

        newAdminEmail = findViewById(R.id.new_admin_email);
        newAdminPassword = findViewById(R.id.new_admin_password);
        addAdminButton = findViewById(R.id.add_admin_button);

        adminEmails = new HashSet<>();
        // You could pass existing emails from AdminSignin through Intent
        // adminEmails = getIntent().getStringArrayListExtra("EXISTING_ADMIN_EMAILS");

        // Get a reference to the Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference("admins");

        addAdminButton.setOnClickListener(v -> addNewAdmin());
    }

    private void addNewAdmin() {
        String email = newAdminEmail.getText().toString().trim();
        String password = newAdminPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            newAdminEmail.setError("Email cannot be empty");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            newAdminPassword.setError("Password cannot be empty");
            return;
        }

        if (adminEmails.contains(email)) {
            Toast.makeText(this, "Admin already exists", Toast.LENGTH_SHORT).show();
        } else {
            // Add the new admin to the set and store the email and password in Firebase
            adminEmails.add(email);
            storeAdminInFirebase(email, password);
            Toast.makeText(this, "New admin added successfully", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("NEW_ADMIN_EMAIL", email);
            setResult(RESULT_OK, resultIntent); // Set result to OK
            finish(); // Close this activity and return to the previous one
        }
    }

    private void storeAdminInFirebase(String email, String password) {
        // Create a new map to store the admin data
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("email", email);
        adminData.put("password", password);

        // Push the new admin data to the "admins" node in the database
        databaseRef.child(email.replace(".", "_")).setValue(adminData);
    }
}