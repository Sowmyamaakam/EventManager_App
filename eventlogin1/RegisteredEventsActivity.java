package com.example.eventlogin1;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisteredEventsActivity extends AppCompatActivity {

    private RecyclerView registeredEventsRecyclerView;
    private DatabaseReference databaseReference;
    private String currentUserEmail;
    private RegisteredEventsAdapter adapter;
    private List<Registration> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_events);

        registeredEventsRecyclerView = findViewById(R.id.registeredEventsRecyclerView);
        databaseReference = FirebaseDatabase.getInstance().getReference("registrations");

        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Initialize the event list and adapter
        eventList = new ArrayList<>();
        adapter = new RegisteredEventsAdapter(this, eventList);

        // Set up RecyclerView
        registeredEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        registeredEventsRecyclerView.setAdapter(adapter);

        // Fetch registered events for the user
        fetchRegisteredEvents();
    }

    private void fetchRegisteredEvents() {
        databaseReference.orderByChild("email").equalTo(currentUserEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        eventList.clear();  // Clear previous data
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Registration registration = snapshot.getValue(Registration.class);
                            if (registration != null) {
                                String eventDate = registration.getSelectedDate();
                                String formattedDate = formatDate(eventDate);
                                registration.setSelectedDate(formattedDate);

                                eventList.add(registration);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(RegisteredEventsActivity.this, "Error fetching events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to format the date as "15 OCT"
    private String formatDate(String dateStr) {
        try {
            // Parse the original date string (e.g., "2024-09-10")
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);

            // Format the date to "dd MMM" (e.g., "15 OCT")
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateStr; // Return original if parsing fails
        }
    }
}
