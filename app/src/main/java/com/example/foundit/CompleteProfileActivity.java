package com.example.foundit;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CompleteProfileActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPhone;
    private Spinner spinnerGender;
    private MaterialButton btnSave;

    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        etUsername = findViewById(R.id.etUsername);
        etPhone = findViewById(R.id.etPhone);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSave = findViewById(R.id.btnSaveProfile);

        // Spinner gender
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Prefer not to say", "Male", "Female"}
        );
        spinnerGender.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) finish();

        userRef = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users")
                .child(currentUserId);

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this,
                    "Username and phone number are required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("phoneNumber", phone);
        userData.put("gender", gender);
        userData.put("createdAt", System.currentTimeMillis());

        userRef.setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Profile completed",
                            Toast.LENGTH_SHORT).show();
                    finish(); // balik ke MainActivity → HomeFragment
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save profile",
                                Toast.LENGTH_SHORT).show());
    }

    // 🚫 BLOCK BACK BUTTON
    @Override
    public void onBackPressed() {
        Toast.makeText(this,
                "Please complete your profile first",
                Toast.LENGTH_SHORT).show();
    }
}
