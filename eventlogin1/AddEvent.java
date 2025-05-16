package com.example.eventlogin1;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class AddEvent extends AppCompatActivity {
    private static final String TAG = "AddEvent";
    private static final String CHANNEL_ID = "event_notification_channel";
    private static final int NOTIFICATION_ID = 1;

    private EditText titleEditText, descriptionEditText, timeEditText, venueEditText, guestSpeakerEditText, registerLinkEditText;
    private TextView dateEditText;
    private Button saveButton, buttonBack, selectImageButton;
    private ImageView eventPosterImageView;
    private Uri imageUri;
    private String eventId;
    private static final int REQUEST_CODE_IMAGE_PICKER = 123;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addevent);

        initializeViews();
        setupListeners();
        createNotificationChannel();

        // Initialize Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("events");

        // Retrieve the eventId from the intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            loadEventData(eventId);
        }
    }

    private void initializeViews() {
        titleEditText = findViewById(R.id.editTextEventTitle);
        descriptionEditText = findViewById(R.id.editTextEventDescription);
        dateEditText = findViewById(R.id.editTextEventDate);
        timeEditText = findViewById(R.id.editTextEventTime);
        venueEditText = findViewById(R.id.editTextEventVenue);
        guestSpeakerEditText = findViewById(R.id.editTextGuestSpeaker);
        registerLinkEditText = findViewById(R.id.editTextRegisterLink);
        eventPosterImageView = findViewById(R.id.imageViewEventPoster);
        saveButton = findViewById(R.id.buttonSaveEvent);
        buttonBack = findViewById(R.id.buttonBack);
        selectImageButton = findViewById(R.id.buttonSelectImage);
    }

    private void setupListeners() {
        dateEditText.setOnClickListener(v -> showDatePickerDialog());
        timeEditText.setOnClickListener(v -> showTimePickerDialog());
        saveButton.setOnClickListener(v -> saveEvent());
        buttonBack.setOnClickListener(v -> finish());
        selectImageButton.setOnClickListener(v -> selectEventPoster());
    }

    private void selectEventPoster() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            eventPosterImageView.setImageURI(imageUri);
        }
    }

    private void saveEvent() {
        if (validateInputs()) {
            String title = titleEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String date = dateEditText.getText().toString();
            String time = timeEditText.getText().toString();
            String venue = venueEditText.getText().toString();
            String guestSpeaker = guestSpeakerEditText.getText().toString();
            String registerLink = registerLinkEditText.getText().toString();

            String localImagePath = null;
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    localImagePath = saveImageToLocalStorage(bitmap, title);
                    Log.d(TAG, "Image saved at: " + localImagePath);
                } catch (IOException e) {
                    Log.e(TAG, "Error saving image", e);
                    Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            Event event = new Event(title, description, date, time, venue, guestSpeaker, registerLink, localImagePath);
            if (eventId != null) {
                event.setEventId(eventId);
                updateEvent(event);
            } else {
                addEvent(event);
            }
        }
    }

    private String saveImageToLocalStorage(Bitmap bitmap, String title) {
        File directory = new File(getFilesDir(), "images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File imageFile = new File(directory, title + "_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            return null;
        }
    }

    private void loadEventData(String eventId) {
        databaseReference.child(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Event event = task.getResult().getValue(Event.class);
                if (event != null) {
                    titleEditText.setText(event.getTitle());
                    descriptionEditText.setText(event.getDescription());
                    dateEditText.setText(event.getDate());
                    timeEditText.setText(event.getTime());
                    venueEditText.setText(event.getVenue());
                    guestSpeakerEditText.setText(event.getGuestSpeaker());
                    registerLinkEditText.setText(event.getRegisterLink());

                    if (event.getLocalImagePath() != null) {
                        Glide.with(this)
                                .load(event.getLocalImagePath())
                                .into(eventPosterImageView);
                    }
                }
            } else {
                Log.e(TAG, "Error loading event data", task.getException());
            }
        });
    }

    private void updateEvent(Event event) {
        databaseReference.child(event.getEventId()).setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddEvent.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating event", e);
                    Toast.makeText(AddEvent.this, "Error updating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addEvent(Event event) {
        String key = databaseReference.push().getKey();
        if (key != null) {
            event.setEventId(key);
            databaseReference.child(key).setValue(event)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddEvent.this, "Event added successfully", Toast.LENGTH_SHORT).show();
                        showNotification(event);  // Display notification
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding event", e);
                        Toast.makeText(AddEvent.this, "Error adding event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private boolean validateInputs() {
        if (titleEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (descriptionEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedMonth++;
                    dateEditText.setText(selectedYear + "-" + selectedMonth + "-" + selectedDay);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    timeEditText.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Channel";
            String description = "Notifications for events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Event event) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Event Added: " + event.getTitle())
                .setContentText("Click to view the event details")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
