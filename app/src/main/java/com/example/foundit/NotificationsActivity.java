package com.example.foundit;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationsActivity extends AppCompatActivity {

    private Switch switchNotifications;
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Tukar warna status bar supaya sama macam background splash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFDE7")); // warna cerah
        }

        // Tukar icon status bar jadi gelap supaya nampak pada background cerah
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        switchNotifications = findViewById(R.id.switchNotifications);

        // --- Firebase reference ---
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance(
                    "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app"
            ).getReference("Users").child(currentUser.getUid());
        }

        // --- Load saved preference locally first ---
        boolean isEnabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(isEnabled);

        // --- Sync toggle ke Firebase bila berubah ---
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // save locally
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();

            // save ke Firebase
            if (userRef != null) {
                userRef.child("notifications_enabled").setValue(isChecked);
            }

            // show toast
            if (isChecked) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
