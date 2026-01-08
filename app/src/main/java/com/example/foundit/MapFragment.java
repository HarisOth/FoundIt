package com.example.foundit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private MapView mapView;
    private FrameLayout popupContainer;
    private DatabaseReference databaseReports;
    private final List<Marker> markerList = new ArrayList<>();
    private final List<ReportItem> reportList = new ArrayList<>();

    private String filterCategory = null;
    private String filterStatus = null;
    private String searchQuery = null;

    private EditText etSearch;
    private ImageView btnFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        popupContainer = view.findViewById(R.id.popupContainer);
        etSearch = view.findViewById(R.id.etSearch);
        btnFilter = view.findViewById(R.id.btnFilter);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    && popupContainer.getChildCount() > 0) {
                popupContainer.removeAllViews();
            }
            return false;
        });

        databaseReports = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("reports");

        loadReports();
        setupListeners();

        return view;
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                applyFiltersAndUpdateMarkers();
            }
        });

        btnFilter.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(getActivity());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.bottom_sheet_filter, null);

        LinearLayout container = view.findViewById(R.id.filterContainer);
        container.removeAllViews();

        // Filters + Reset
        String[] filters = {"Lost", "Found", "Open", "Claimed", "Resolved", "Reset All"};

        for (String f : filters) {
            TextView tv = new TextView(getContext());
            tv.setText(f);
            tv.setTextSize(16f);
            tv.setPadding(24, 24, 24, 24);
            tv.setTextColor(getResources().getColor(android.R.color.black, null));

            tv.setOnClickListener(v -> {
                if (f.equalsIgnoreCase("Lost") || f.equalsIgnoreCase("Found")) {
                    filterCategory = f;
                } else if (f.equalsIgnoreCase("Open") || f.equalsIgnoreCase("Claimed") || f.equalsIgnoreCase("Resolved")) {
                    filterStatus = f;
                } else if (f.equalsIgnoreCase("Reset All")) {
                    filterCategory = null;
                    filterStatus = null;
                    searchQuery = null;
                    etSearch.setText("");
                }
                applyFiltersAndUpdateMarkers();
                sheetDialog.dismiss();
            });

            container.addView(tv);
        }

        sheetDialog.setContentView(view);
        sheetDialog.show();
    }

    private void loadReports() {
        databaseReports.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ReportItem report = ds.getValue(ReportItem.class);
                    if (report != null) reportList.add(report);
                }
                applyFiltersAndUpdateMarkers();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyFiltersAndUpdateMarkers() {
        mapView.getOverlays().clear();
        markerList.clear();
        popupContainer.removeAllViews();

        List<GeoPoint> allPoints = new ArrayList<>();

        for (ReportItem report : reportList) {
            if (filterCategory != null && !filterCategory.equalsIgnoreCase(report.getCategory()))
                continue;
            if (filterStatus != null && !filterStatus.equalsIgnoreCase(report.getStatus()))
                continue;
            if (searchQuery != null && !searchQuery.isEmpty() &&
                    (report.getItemName() == null || !report.getItemName().toLowerCase().contains(searchQuery.toLowerCase())))
                continue;

            GeoPoint point = new GeoPoint(report.getLatitude(), report.getLongitude());
            allPoints.add(point);

            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            if ("Lost".equalsIgnoreCase(report.getCategory())) {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_marker_red));
            } else {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_marker_green));
            }

            marker.setOnMarkerClickListener((m, mapView1) -> {
                showPopupBottom(report);
                return true;
            });

            mapView.getOverlays().add(marker);
            markerList.add(marker);
        }

        if (!allPoints.isEmpty()) {
            double minLat = allPoints.get(0).getLatitude();
            double maxLat = minLat;
            double minLon = allPoints.get(0).getLongitude();
            double maxLon = minLon;
            for (GeoPoint p : allPoints) {
                minLat = Math.min(minLat, p.getLatitude());
                maxLat = Math.max(maxLat, p.getLatitude());
                minLon = Math.min(minLon, p.getLongitude());
                maxLon = Math.max(maxLon, p.getLongitude());
            }
            BoundingBox box = new BoundingBox(maxLat + 0.001, maxLon + 0.001,
                    minLat - 0.001, minLon - 0.001);
            mapView.zoomToBoundingBox(box, true);
        }

        mapView.invalidate();
    }

    private void showPopupBottom(ReportItem report) {
        popupContainer.removeAllViews();

        View popupCard = LayoutInflater.from(getContext())
                .inflate(R.layout.item_mini_card_popup, popupContainer, false);

        ImageView ivCardPhoto = popupCard.findViewById(R.id.imgItem);
        TextView tvName = popupCard.findViewById(R.id.tvItemName);
        TextView tvLocation = popupCard.findViewById(R.id.tvLocation);
        TextView tvContact = popupCard.findViewById(R.id.tvContact);
        TextView tvDate = popupCard.findViewById(R.id.tvDate);
        TextView tvStatus = popupCard.findViewById(R.id.tvStatus);
        TextView tvCategory = popupCard.findViewById(R.id.tvCategory);

        tvName.setText(safe(report.getItemName()));
        tvLocation.setText(safe(report.getLocation()));
        tvContact.setText(safe(report.getContact()));
        tvDate.setText(safe(report.getDate()));

        if ("Lost".equalsIgnoreCase(report.getCategory())) {
            tvCategory.setText("LOST");
            tvCategory.setBackgroundResource(R.drawable.bg_category_lost);
        } else {
            tvCategory.setText("FOUND");
            tvCategory.setBackgroundResource(R.drawable.bg_category_found);
        }

        String status = report.getStatus();
        if ("CLAIMED".equalsIgnoreCase(status)) {
            tvStatus.setText("CLAIMED");
            tvStatus.setBackgroundResource(R.drawable.bg_status_claimed);
        } else if ("RESOLVED".equalsIgnoreCase(status)) {
            tvStatus.setText("RESOLVED");
            tvStatus.setBackgroundResource(R.drawable.bg_status_resolved);
        } else {
            tvStatus.setText("OPEN");
            tvStatus.setBackgroundResource(R.drawable.bg_status_open);
        }

        if (report.getImageBase64() != null && !report.getImageBase64().isEmpty()) {
            try {
                byte[] decoded = Base64.decode(report.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                ivCardPhoto.setImageBitmap(bitmap);
                ivCardPhoto.setOnClickListener(v -> {
                    ImagePreviewFragment preview =
                            ImagePreviewFragment.newInstance(report.getImageBase64());
                    preview.show(getParentFragmentManager(), "preview");
                });
            } catch (Exception e) {
                ivCardPhoto.setImageResource(R.drawable.bg_popup_card);
            }
        } else {
            ivCardPhoto.setImageResource(R.drawable.bg_popup_card);
        }

        popupContainer.addView(popupCard);
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }
}
