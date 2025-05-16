package com.example.eventlogin1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

public class AdminSignin extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private FirebaseAuth auth;
    private TextView forgotPassword;
    private Set<String> adminEmails;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminlogin);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        forgotPassword = findViewById(R.id.forgot_password);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("admins");

        adminEmails = new HashSet<>();
        fetchAdminsFromFirebase();

        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void fetchAdminsFromFirebase() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adminEmails.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    adminEmails.add(email);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    private void handleLogin() {
        String email = loginEmail.getText().toString().trim();
        String pass = loginPassword.getText().toString();

        if (adminEmails.contains(email)) {
            if (!pass.isEmpty()) {
                if (pass.equals("123456")) { // Replace with actual password validation
                    Toast.makeText(AdminSignin.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdminSignin.this, AdminDashboard.class));
                    finish();
                } else {
                    Toast.makeText(AdminSignin.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                }
            } else {
                loginPassword.setError("Empty fields are not allowed");
            }
        } else {
            loginEmail.setError("You are not an admin");
        }
    }
}