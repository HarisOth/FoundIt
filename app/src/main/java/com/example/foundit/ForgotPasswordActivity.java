package com.example.foundit;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Tukar warna status bar supaya sama macam background splash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFDE7"));
        }

        // Tukar icon status bar jadi gelap supaya nampak pada background cerah
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        etEmail = findViewById(R.id.etEmail);
        btnReset = findViewById(R.id.btnReset);

        btnReset.setOnClickListener(v -> {
            // Placeholder action
            Toast.makeText(ForgotPasswordActivity.this,
                    "Coming soon! Feature under development.", Toast.LENGTH_SHORT).show();
        });
    }
}
