package com.example.foundit;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Tukar warna status bar supaya sama macam background splash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFDE7")); // warna cerah
        }

        // Tukar icon status bar jadi gelap supaya nampak pada background cerah
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        TextView tvAbout = findViewById(R.id.tvAbout);
        tvAbout.setText("FoundIt App\nVersion 1.0\n\nLost and Found Platform");
    }
}