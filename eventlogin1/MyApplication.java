package com.example.eventlogin1;

import android.app.Application;
import android.os.Build;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Check the Android version to ensure the app handles UI/UX correctly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // You could also use this to enable/disable Night Mode dynamically based on system-wide preferences.
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);  // Force Light Mode
        } else {
            // Older versions (below Android Pie), continue with default settings
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Optionally, if you want to respect user preferences, you can load a shared preference or system settings
        // For now, we force Light Mode. If you want to toggle between Light and Dark modes based on system preference,
        // you could replace MODE_NIGHT_NO with MODE_NIGHT_FOLLOW_SYSTEM.

        // Other initialization tasks for your app (for example, analytics, crash reporting, etc.)
    }
}

