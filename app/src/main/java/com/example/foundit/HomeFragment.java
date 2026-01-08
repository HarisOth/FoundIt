package com.example.foundit;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private LinearLayout cardLost, cardFound;
    private EditText etSearch;
    private RecyclerView recyclerReports;
    private FloatingActionButton fabAddReport;
    private View btnFilter;

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

        cardLost = view.findViewById(R.id.cardLost);
        cardFound = view.findViewById(R.id.cardFound);
        etSearch = view.findViewById(R.id.etSearch);
        recyclerReports = view.findViewById(R.id.recyclerReports);
        fabAddReport = view.findViewById(R.id.fabAddReport);
        btnFilter = view.findViewById(R.id.btnFilter);

        reportList = new ArrayList<>();
        filteredList = new ArrayList<>();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        adapter = new ReportAdapter(getActivity(), filteredList, currentUserId);
        recyclerReports.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReports.setAdapter(adapter);

        databaseReports = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("reports");

        loadReportsFromFirebase();
        setupListeners();

        return view;
    }

    private void setupListeners() {
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

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }
        });

        fabAddReport.setOnClickListener(v -> showAddEditOptions());
        btnFilter.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showAddEditOptions() {
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_edit, null);
        sheetView.findViewById(R.id.btnAddReport).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ReportActivity.class)));
        sheetView.findViewById(R.id.btnEditReport).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditReportActivity.class)));

        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(sheetView);
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
                sortByLatest(); // default
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterByCategory(String category) {
        filteredList.clear();
        for (ReportItem item : reportList) {
            if (item.getCategory() != null &&
                    item.getCategory().equalsIgnoreCase(category)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void filterBySearch(String query) {
        filteredList.clear();
        String q = query.toLowerCase();
        for (ReportItem item : reportList) {
            if (item.getItemName() != null &&
                    item.getItemName().toLowerCase().contains(q)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(getActivity());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);

        LinearLayout container = view.findViewById(R.id.filterContainer);
        container.removeAllViews();

        String[] filters = {"Latest", "Rating", "Open", "Claimed", "Resolved"};
        for (String f : filters) {
            TextView tv = new TextView(getContext());
            tv.setText(f);
            tv.setTextSize(16f);
            tv.setPadding(24, 24, 24, 24);
            tv.setTextColor(getResources().getColor(android.R.color.black, null));
            tv.setOnClickListener(v -> {
                applyFilter(f);
                sheetDialog.dismiss();
            });
            container.addView(tv);
        }

        sheetDialog.setContentView(view);
        sheetDialog.show();
    }

    private void applyFilter(String filter) {
        switch (filter) {
            case "Latest":
                sortByLatest();
                break;
            case "Rating":
                sortByRating();
                break;
            case "Open":
            case "Claimed":
            case "Resolved":
                filterByStatus(filter.toLowerCase());
                break;
        }
    }

    private void sortByLatest() {
        filteredList.clear();
        filteredList.addAll(reportList);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Collections.sort(filteredList, (a, b) -> {
            try {
                Date d1 = sdf.parse(a.getDate());
                Date d2 = sdf.parse(b.getDate());
                if (d1 == null || d2 == null) return 0;
                return d2.compareTo(d1);
            } catch (ParseException e) {
                return 0;
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void sortByRating() {
        filteredList.clear();
        filteredList.addAll(reportList);
        Collections.sort(filteredList, (a, b) -> Integer.compare(b.getLikesCount(), a.getLikesCount()));
        adapter.notifyDataSetChanged();
    }

    private void filterByStatus(String status) {
        filteredList.clear();
        for (ReportItem item : reportList) {
            if (item.getStatus() != null &&
                    item.getStatus().equalsIgnoreCase(status)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
