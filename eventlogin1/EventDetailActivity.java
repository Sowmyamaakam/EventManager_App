package com.example.eventlogin1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class EventDetailActivity extends AppCompatActivity {

    private TextView titleTextView, dateTextView, timeTextView, speakerTextView, eventDecr;
    private ImageView eventImageView;
    private Button registerButton;
    private DatabaseReference databaseReference;
    private String currentUserEmail;
    private String eventId;  // Variable to store eventId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        titleTextView = findViewById(R.id.eventTitle);
        dateTextView = findViewById(R.id.dateText);
        timeTextView = findViewById(R.id.timeText);
        speakerTextView = findViewById(R.id.speakerText);
        eventImageView = findViewById(R.id.eventImageView);
        eventDecr = findViewById(R.id.eventDescription);
        registerButton = findViewById(R.id.registerButton);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String speaker = intent.getStringExtra("guestSpeaker");
        String localImagePath = intent.getStringExtra("localimagepath");
        String description = intent.getStringExtra("description");

        titleTextView.setText(title);
        dateTextView.setText(date);
        timeTextView.setText(time);
        speakerTextView.setText(speaker);
        eventDecr.setText(description);

        if (localImagePath != null && !localImagePath.isEmpty()) {
            File imageFile = new File(localImagePath);
            if (imageFile.exists()) {
                Glide.with(this)
                        .load(imageFile)
                        .error(R.drawable.default_image)
                        .into(eventImageView);
            } else {
                eventImageView.setImageResource(R.drawable.default_image);
                Toast.makeText(this, "Image file not found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            eventImageView.setImageResource(R.drawable.default_image);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();

        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        fetchEventId(title);
    }

    private void fetchEventId(String eventTitle) {
        databaseReference.child("events")
                .orderByChild("title")
                .equalTo(eventTitle)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                eventId = snapshot.getKey();  // Fetch the event ID from Firebase
                                checkRegistrationStatus(eventId, eventTitle);  // Check registration status using eventId
                                break;
                            }
                        } else {
                            Toast.makeText(EventDetailActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EventDetailActivity.this, "Failed to fetch event ID.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkRegistrationStatus(String eventId, String eventTitle) {
        if (currentUserEmail == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("registrations")
                .orderByChild("email")
                .equalTo(currentUserEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean isRegistered = false;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String registeredEventId = snapshot.child("eventId").getValue(String.class);

                            if (eventId.equals(registeredEventId)) {
                                isRegistered = true;
                                break;
                            }
                        }

                        if (isRegistered) {
                            registerButton.setEnabled(false);
                            registerButton.setText("Already Registered");
                            Toast.makeText(EventDetailActivity.this, "You have already registered for this event.", Toast.LENGTH_SHORT).show();
                        } else {
                            registerButton.setEnabled(true);
                            registerButton.setText("Register");
                            registerButton.setOnClickListener(v -> {
                                Intent registerIntent = new Intent(EventDetailActivity.this, EventRegistrationActivity.class);
                                registerIntent.putExtra("selectedEventId", eventId);  // Changed key to match
                                registerIntent.putExtra("eventname", titleTextView.getText().toString());  // Changed key to match
                                registerIntent.putExtra("eventDate", dateTextView.getText().toString());
                                registerIntent.putExtra("eventtime", timeTextView.getText().toString());
                                startActivity(registerIntent);
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EventDetailActivity.this, "Failed to check registration status.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
