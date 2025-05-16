package com.example.eventlogin1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StudentDashboard extends AppCompatActivity {
    private TextView textViewUserEmail;
    private Switch toggleEvents;
    private Calendar calendar;
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private static final int DOUBLE_CLICK_DELAY = 300;
    private Handler handler = new Handler();
    private Runnable clickRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_dashboard);

        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        toggleEvents = findViewById(R.id.toggleEvents);
        calendar = Calendar.getInstance();
        bottomNavigation = findViewById(R.id.bottomNavigation);
        recyclerView = findViewById(R.id.recyclerView);

        // Set the initial state of the toggle button to ON (events are displayed by default)
        toggleEvents.setChecked(true);

        setupBottomNavigation();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        // Display events based on the initial state of the toggle button
        displayEvents(true); // Display events by default if the switch is ON

        toggleEvents.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only show events if the switch is turned ON
            displayEvents(isChecked);
        });

        displayUserEmail();
        updateMonthText();
    }

    private void displayUserEmail() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            textViewUserEmail.setText(email);
        } else {
            textViewUserEmail.setText("No user logged in");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayEvents(toggleEvents.isChecked()); // Show events based on the current state of the toggle switch
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().detachListener();
        if (handler != null && clickRunnable != null) {
            handler.removeCallbacks(clickRunnable);
        }
    }

    private void displayEvents(boolean isVisible) {
        if (isVisible) {
            // Show events if the switch is ON
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("events");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Event> events = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Event event = dataSnapshot.getValue(Event.class);
                        if (event != null) {
                            event.setDate(formatEventDate(event.getDate()));
                            events.add(event);
                        }
                    }

                    EventAdapter adapter = new EventAdapter(events, StudentDashboard.this);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);  // Ensure RecyclerView is visible
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StudentDashboard.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Hide events if the switch is OFF
            recyclerView.setVisibility(View.GONE);  // Hide RecyclerView when events are not shown
        }
    }

    private String formatEventDate(String eventDate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return displayFormat.format(originalFormat.parse(eventDate));
        } catch (Exception e) {
            e.printStackTrace();
            return eventDate;
        }
    }

    private void updateMonthText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                Intent addEventIntent = new Intent(StudentDashboard.this, StudentDashboard.class);
                startActivity(addEventIntent);
                return true;
            } else if (itemId == R.id.action_registered_events) {
                Intent addEventIntent = new Intent(StudentDashboard.this, RegisteredEventsActivity.class);
                startActivity(addEventIntent);
                return true;

            } else if (itemId == R.id.action_logout) {
                Toast.makeText(StudentDashboard.this, "Logged out", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }
            return false;
        });
    }
}
