package com.example.eventlogin1;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "EventRegistration";
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private static final String NOTIFICATION_CHANNEL_ID = "EVENT_REMINDER_CHANNEL";

    private EditText fullnameEditText, sapIdEditText, emailEditText, mobileEditText, yearEditText;
    private Button submitButton;
    private String EventId;
    private String selectedDate;
    private String eventtime;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_registration);

        initializeViews();
        getEventDataFromIntent();

        databaseReference = FirebaseDatabase.getInstance().getReference("registrations");

        requestNotificationPermission();
        createNotificationChannel();

        submitButton.setOnClickListener(v -> registerForEvent());
    }

    private void initializeViews() {
        fullnameEditText = findViewById(R.id.fullnameEditText);
        sapIdEditText = findViewById(R.id.sapIdEditText);
        emailEditText = findViewById(R.id.emailEditText);
        mobileEditText = findViewById(R.id.mobileEditText);
        yearEditText = findViewById(R.id.yearEditText);
        submitButton = findViewById(R.id.submitButton);
    }

    private void getEventDataFromIntent() {
        EventId = getIntent().getStringExtra("selectedEventId");
        String eventTitle = getIntent().getStringExtra("eventname");
        selectedDate = getIntent().getStringExtra("eventDate");
        eventtime = getIntent().getStringExtra("eventtime");

        Log.d(TAG, "Event Details - ID: " + EventId + ", Title: " + eventTitle +
                ", Date: " + selectedDate + ", Time: " + eventtime);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission is required for event reminders",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registerForEvent() {
        String fullname = fullnameEditText.getText().toString().trim();
        String sapId = sapIdEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String year = yearEditText.getText().toString().trim();
        String eventTitle = getIntent().getStringExtra("eventname");

        if (!validateInputFields(fullname, sapId, email, mobile, year)) {
            return;
        }

        Registration registration = new Registration(selectedDate, fullname, sapId, email,
                mobile, year, EventId, eventTitle);

        saveRegistrationToFirebase(registration, eventTitle);
    }

    private boolean validateInputFields(String fullname, String sapId, String email,
                                        String mobile, String year) {
        if (TextUtils.isEmpty(fullname)) {
            fullnameEditText.setError("Please enter your full name");
            return false;
        }
        if (TextUtils.isEmpty(sapId)) {
            sapIdEditText.setError("Please enter your SAP ID");
            return false;
        }
        if (TextUtils.isEmpty(email) || !isValidEmail(email)) {
            emailEditText.setError("Please enter a valid email address");
            return false;
        }
        if (TextUtils.isEmpty(mobile) || !isValidMobile(mobile)) {
            mobileEditText.setError("Please enter a valid 10-digit mobile number");
            return false;
        }
        if (TextUtils.isEmpty(year)) {
            yearEditText.setError("Please enter your year");
            return false;
        }
        return true;
    }

    private void saveRegistrationToFirebase(Registration registration, String eventTitle) {
        databaseReference.push().setValue(registration, (error, ref) -> {
            if (error == null) {
                Toast.makeText(EventRegistrationActivity.this,
                        "Registration Successful!", Toast.LENGTH_SHORT).show();
                scheduleEventReminder(eventTitle);
                finish();
            } else {
                Toast.makeText(EventRegistrationActivity.this,
                        "Registration Failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Firebase registration failed", error.toException());
            }
        });
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidMobile(String mobile) {
        return mobile.length() == 10 && mobile.matches("\\d+");
    }

    private void scheduleEventReminder(String eventTitle) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        try {
            Calendar eventDateTime = Calendar.getInstance();
            eventDateTime.setTime(dateFormat.parse(selectedDate + " " + eventtime));

            long delayInMillis = eventDateTime.getTimeInMillis() - System.currentTimeMillis()
                    - TimeUnit.MINUTES.toMillis(1);

            if (delayInMillis > 0) {
                scheduleWorkManager(eventTitle, delayInMillis);
            } else {
                Log.w(TAG, "Event time is in the past or too close to schedule reminder");
                Toast.makeText(this, "Event is too close or has passed to set reminder",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing event date/time", e);
            Toast.makeText(this, "Error scheduling reminder", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleWorkManager(String eventTitle, long delayInMillis) {
        Data reminderData = new Data.Builder()
                .putString("EVENT_NAME", eventTitle)
                .build();

        WorkRequest reminderWork = new OneTimeWorkRequest.Builder(ScheduleReminderWorker.class)
                .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                .setInputData(reminderData)
                .addTag("event_reminder_" + eventTitle)
                .build();

        WorkManager.getInstance(this).enqueue(reminderWork);
        Log.d(TAG, "Reminder scheduled for event: " + eventTitle +
                " with work ID: " + reminderWork.getId());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Event Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for event reminders");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }
}
