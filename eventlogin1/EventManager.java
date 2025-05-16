package com.example.eventlogin1;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private static EventManager instance;
    private final DatabaseReference eventsRef;
    private List<Event> events;
    private ValueEventListener eventListener;

    // Private constructor (Singleton pattern)
    private EventManager() {
        events = new ArrayList<>();
        eventsRef = FirebaseDatabase.getInstance().getReference("events");
        setupEventListener();  // Set up Firebase listener to retrieve events
    }

    // Singleton pattern: Return a single instance of EventManager
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    // Set up Firebase event listener
    private void setupEventListener() {
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        event.setEventId(eventSnapshot.getKey());
                        events.add(event);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error: " + error.getMessage());
            }
        };
        eventsRef.addValueEventListener(eventListener);
    }

    // Add a new event to Firebase
    public void addEvent(Event event, OnEventCallback callback) {
        String eventId = eventsRef.push().getKey();  // Generate a new unique key
        if (eventId != null) {
            event.setEventId(eventId);
            // Push event to Firebase and handle success/failure
            eventsRef.child(eventId).setValue(event)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("Event ID generation failed"));
        }
    }

    // Update an existing event in Firebase
    public void updateEvent(Event event, OnEventCallback callback) {
        if (event.getEventId() != null) {
            // Update event data in Firebase
            eventsRef.child(event.getEventId()).setValue(event)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("Event ID is null"));
        }
    }

    // Remove an event from Firebase
    public void removeEvent(Event event, OnEventCallback callback) {
        if (event.getEventId() != null) {
            // Remove event from Firebase
            eventsRef.child(event.getEventId()).removeValue()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("Event ID is null"));
        }
    }

    // Get the list of all events (this list is populated via Firebase listener)
    public List<Event> getEvents() {
        return events;
    }

    // Fetch an event by its ID
    public Event getEventById(String eventId) {
        for (Event event : events) {
            if (event.getEventId().equals(eventId)) {
                return event;  // Return the matching event
            }
        }
        return null;  // Return null if no event is found
    }

    // Detach the Firebase event listener when it's no longer needed
    public void detachListener() {
        if (eventListener != null) {
            eventsRef.removeEventListener(eventListener);  // Remove the listener
        }
    }

    public interface OnEventCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void sendEventNotification(String title, String message) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        // Create notification data
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());

        // Save notification to Firebase
        notificationsRef.push().setValue(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving notification", e);
                });
    }
}
