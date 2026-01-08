package com.example.foundit;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class EditReportActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditReportAdapter adapter;
    ArrayList<ReportItem> reportList;
    DatabaseReference databaseReports;
    LinearLayout layoutMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_report);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFDE7")); // warna cerah
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        layoutMain = findViewById(R.id.layoutMain);

        recyclerView = findViewById(R.id.recyclerEditReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reportList = new ArrayList<>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        adapter = new EditReportAdapter(this, reportList, report -> {
            if (report == null) return;

            // Hide main layout (title + list)
            layoutMain.setVisibility(View.GONE);

            // Show fragment container
            findViewById(R.id.main_container).setVisibility(View.VISIBLE);

            EditReportFragment fragment = new EditReportFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("report", report);
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        databaseReports = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("reports");

        loadUserReports(currentUserId);
    }

    private void loadUserReports(String userId) {
        databaseReports.orderByChild("ownerId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reportList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            ReportItem item = snap.getValue(ReportItem.class);
                            if (item != null) {
                                if (item.getItemName() == null) item.setItemName("No Name");
                                if (item.getDescription() == null) item.setDescription("No Description");
                                reportList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (reportList.isEmpty()) {
                            Toast.makeText(EditReportActivity.this, "You haven't created any reports yet 📝", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditReportActivity.this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        // If fragment visible, hide it and show main layout
        View fragmentContainer = findViewById(R.id.main_container);
        if (fragmentContainer.getVisibility() == View.VISIBLE) {
            fragmentContainer.setVisibility(View.GONE);
            layoutMain.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
