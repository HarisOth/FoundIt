package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private LinearLayout cardLost, cardFound;
    private EditText etSearch;
    private RecyclerView recyclerReports;
    private FloatingActionButton fabAddReport;

    private ReportAdapter adapter;
    private ArrayList<ReportItem> reportList;
    private ArrayList<ReportItem> filteredList;

    private DatabaseReference databaseReports;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        cardLost = view.findViewById(R.id.cardLost);
        cardFound = view.findViewById(R.id.cardFound);
        etSearch = view.findViewById(R.id.etSearch);
        recyclerReports = view.findViewById(R.id.recyclerReports);
        fabAddReport = view.findViewById(R.id.fabAddReport);

        // Init lists
        reportList = new ArrayList<>();
        filteredList = new ArrayList<>();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        // Adapter
        adapter = new ReportAdapter(getActivity(), filteredList, currentUserId);
        recyclerReports.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReports.setAdapter(adapter);

        // Firebase reference
        databaseReports = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("reports");

        loadReportsFromFirebase();
        setupListeners();

        return view;
    }

    private void setupListeners() {
        // Lost / Found filter
        cardLost.setOnClickListener(v -> {
            cardLost.setSelected(true);
            cardFound.setSelected(false);
            filterByCategory("Lost");
        });

        cardFound.setOnClickListener(v -> {
            cardFound.setSelected(true);
            cardLost.setSelected(false);
            filterByCategory("Found");
        });

        // Search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // FAB + button open Add/Edit options
        fabAddReport.setOnClickListener(v -> showAddEditOptions());
    }

    private void showAddEditOptions() {
        // Inflate bottom sheet layout
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_edit, null);

        sheetView.findViewById(R.id.btnAddReport).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ReportActivity.class));
        });

        sheetView.findViewById(R.id.btnEditReport).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditReportActivity.class));
        });

        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(sheetView);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void loadReportsFromFirebase() {
        databaseReports.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ReportItem item = snap.getValue(ReportItem.class);
                    if (item != null) reportList.add(item);
                }
                // Default: show all
                filteredList.clear();
                filteredList.addAll(reportList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterByCategory(String category) {
        filteredList.clear();
        for (ReportItem item : reportList) {
            if (item.getCategory() != null && item.getCategory().equalsIgnoreCase(category)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void filterBySearch(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase();
        for (ReportItem item : reportList) {
            if (item.getItemName() != null && item.getItemName().toLowerCase().contains(lowerQuery)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
