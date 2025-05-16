package com.example.eventlogin1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminDashboard extends AppCompatActivity {
    private TextView textViewMonth;
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
        setContentView(R.layout.admin_dashboard);

        textViewMonth = findViewById(R.id.textViewMonth);
        toggleEvents = findViewById(R.id.toggleEvents);
        calendar = Calendar.getInstance();
        bottomNavigation = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); // Ensure this class exists

        updateMonthText(calendar.get(Calendar.MONTH));

        // Set up the toggle listener to display events based on the toggle state
        toggleEvents.setOnCheckedChangeListener((buttonView, isChecked) -> displayEvents());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                Intent addEventIntent = new Intent(AdminDashboard.this, AdminDashboard.class);
                startActivity(addEventIntent);
                return true;
            } else if (itemId == R.id.action_add_event) {
                Intent addEventIntent = new Intent(AdminDashboard.this, AddEvent.class);
                startActivity(addEventIntent);
                return true;
            } else if (itemId == R.id.action_add_admin) {
                Intent addAdminIntent = new Intent(AdminDashboard.this, AddAdmin.class);
                startActivity(addAdminIntent);
                return true;
            } else if (itemId == R.id.action_logout) {
                Toast.makeText(AdminDashboard.this, "Logged out", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().detachListener();
        if (handler != null && clickRunnable != null) {
            handler.removeCallbacks(clickRunnable);
        }
    }

    private void displayEvents() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("events");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Event> events = new ArrayList<>();

                // Loop through all events in the database and add to the list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event = dataSnapshot.getValue(Event.class);
                    if (event != null) {
                        events.add(event);
                    }
                }

                // Set up the adapter with the updated list
                EventAdapter adapter = new EventAdapter(events, AdminDashboard.this);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboard.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateMonthText(int month) {
        String monthName = calendar.getDisplayName(
                Calendar.MONTH,
                Calendar.LONG,
                getResources().getConfiguration().locale
        );
        textViewMonth.setText(monthName); // Only display the month name, without the year
    }

    public void confirmDeleteEvent(Event event) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    EventManager.getInstance().removeEvent(event, new EventManager.OnEventCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(AdminDashboard.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                            displayEvents(); // Refresh the events list
                            EventAdapter adapter = (EventAdapter) recyclerView.getAdapter();
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(AdminDashboard.this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
