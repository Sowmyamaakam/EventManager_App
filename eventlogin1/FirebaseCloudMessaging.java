package com.example.eventlogin1;

import android.util.Log;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirebaseCloudMessaging {
    private static final String TAG = "FirebaseCloudMessaging";
    private static final String SERVER_KEY = "5258525607938286395";
    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";

    public static void sendNotification(String title, String message) {
        new Thread(() -> {
            try {
                JSONObject notification = new JSONObject();
                JSONObject notificationBody = new JSONObject();
                notificationBody.put("title", title);
                notificationBody.put("body", message);
                notification.put("notification", notificationBody);
                notification.put("to", "/topics/all_users");

                // Create request
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        notification.toString()
                );

                Request request = new Request.Builder()
                        .url(FCM_API)
                        .post(body)
                        .addHeader("Authorization", "key=" + SERVER_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Send request
                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    Log.d(TAG, "Notification sent successfully");
                } else {
                    Log.e(TAG, "Failed to send notification: " + response.body().string());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending notification", e);
            }
        }).start();
    }
}