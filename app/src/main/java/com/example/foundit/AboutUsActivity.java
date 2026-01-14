package com.example.foundit;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFDE7"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setupAppDescription();
        setupTeamMembers();
        setupGitHubLink();
    }

    private void setupAppDescription() {
        TextView tvAbout = findViewById(R.id.tvAbout);

        String description = "FoundIt is a mobile application designed to streamline " +
                "the process of locating lost items and reporting found belongings.\n\n" +
                "Our mission is to help people reunite with their lost items through " +
                "community cooperation and smart technology.";

        tvAbout.setText(description);
    }

    private void setupTeamMembers() {
        // Team Member 1
        TextView tvMember1Name = findViewById(R.id.tvMember1Name);
        TextView tvMember1ID = findViewById(R.id.tvMember1ID);

        tvMember1Name.setText("MOHAMMAD HARIS HAIQAL BIN OTHMAN");
        tvMember1ID.setText("Student ID: 2023213578");

        // Team Member 2
        TextView tvMember2Name = findViewById(R.id.tvMember2Name);
        TextView tvMember2ID = findViewById(R.id.tvMember2ID);

        tvMember2Name.setText("NUR SYAFI'AH ADRIENA BINTI ALIDZA");
        tvMember2ID.setText("Student ID: 2023689932");

        // Team Member 3
        TextView tvMember3Name = findViewById(R.id.tvMember3Name);
        TextView tvMember3ID = findViewById(R.id.tvMember3ID);

        tvMember3Name.setText("NUR AFIQAH BINTI NOORDIN");
        tvMember3ID.setText("Student ID: 2023299132");

        // Team Member 4
        TextView tvMember4Name = findViewById(R.id.tvMember4Name);
        TextView tvMember4ID = findViewById(R.id.tvMember4ID);

        tvMember4Name.setText("MAS AZRA ATHIRAH BINTI MOHD AZAHARI");
        tvMember4ID.setText("Student ID: 2023436008");
    }

    private void setupGitHubLink() {
        LinearLayout llGitHub = findViewById(R.id.llGitHub);
        TextView tvGitHubLink = findViewById(R.id.tvGitHubLink);

        final String githubUrl = "https://github.com/HarisOth/FoundIt";

        // Setup click listener
        llGitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGitHubRepository(githubUrl);
            }
        });

        tvGitHubLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGitHubRepository(githubUrl);
            }
        });

        // Jika tidak ada gambar ic_github, ganti dengan emoji
        try {
            ImageView ivGitHub = (ImageView) ((LinearLayout) llGitHub).getChildAt(0);
            if (ivGitHub.getDrawable() == null) {
                LinearLayout parent = (LinearLayout) ivGitHub.getParent();
                int index = parent.indexOfChild(ivGitHub);
                parent.removeView(ivGitHub);

                TextView tvGitHubEmoji = new TextView(this);
                tvGitHubEmoji.setText("🐙");
                tvGitHubEmoji.setTextSize(40);
                tvGitHubEmoji.setGravity(android.view.Gravity.CENTER);
                tvGitHubEmoji.setLayoutParams(ivGitHub.getLayoutParams());

                parent.addView(tvGitHubEmoji, index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGitHubRepository(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open browser. Please check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}