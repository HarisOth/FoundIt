package com.example.foundit;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    private TextView userEmail;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference notificationsRef;
    private BadgeDrawable notificationsBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Request notification permission for Android 13+
        requestNotificationPermission();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        userEmail = findViewById(R.id.userEmail);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (user != null) {
            userEmail.setText(user.getEmail());
            setupNotificationBadge();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadFragment(new HomeFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) fragment = new HomeFragment();
            else if (id == R.id.nav_map) fragment = new MapFragment();
            else if (id == R.id.nav_notifications) {
                fragment = new AlertsFragment();
                clearNotificationBadge();
            } else if (id == R.id.nav_profile) fragment = new ProfileFragment();

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

        setupBackPressHandler();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == NOTIFICATION_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Notification permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    private void setupNotificationBadge() {
        notificationsBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifications);
        notificationsBadge.setVisible(false);
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        if(user == null) return;

        notificationsRef = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("notifications")
                .child(user.getUid());

        notificationsRef.orderByChild("read").equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long unread = snapshot.getChildrenCount();
                        if(unread > 0){
                            notificationsBadge.setVisible(true);
                            notificationsBadge.setNumber((int)unread);
                        } else notificationsBadge.setVisible(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MainActivity","Failed to load notifications: "+error.getMessage());
                        Toast.makeText(MainActivity.this,"Failed to load notifications",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearNotificationBadge() {
        if(notificationsBadge != null){
            notificationsBadge.setVisible(false);
            notificationsBadge.clearNumber();
        }

        if(user!=null && notificationsRef!=null){
            notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds: snapshot.getChildren()){
                        ds.getRef().child("read").setValue(true);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){
            private long lastBack=0;
            @Override
            public void handleOnBackPressed() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                if(f instanceof HomeFragment){
                    if(System.currentTimeMillis() - lastBack < 2000) finishAffinity();
                    else lastBack=System.currentTimeMillis();
                } else bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        });
    }

    public void signout(android.view.View v){
        auth.signOut();
        startActivity(new Intent(this,LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
