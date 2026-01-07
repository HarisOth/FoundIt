package com.example.foundit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private MapView mapView;
    private FrameLayout popupContainer;
    private DatabaseReference databaseReports;
    private List<Marker> markerList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        popupContainer = view.findViewById(R.id.popupContainer);

        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Tap luar popup → hilang
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && popupContainer.getChildCount() > 0) {
                popupContainer.removeAllViews();
            }
            return false;
        });

        // Firebase
        databaseReports = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("reports");

        loadReports();

        return view;
    }

    private void loadReports() {
        databaseReports.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mapView.getOverlays().clear();
                markerList.clear();
                popupContainer.removeAllViews();

                List<GeoPoint> allPoints = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ReportItem report = ds.getValue(ReportItem.class);
                    if (report == null) continue;

                    GeoPoint point = new GeoPoint(report.getLatitude(), report.getLongitude());
                    allPoints.add(point);

                    Marker marker = new Marker(mapView);
                    marker.setPosition(point);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    // Icon marker
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

                // Fit map to all markers
                if (!allPoints.isEmpty()) {
                    double minLat = allPoints.get(0).getLatitude();
                    double maxLat = allPoints.get(0).getLatitude();
                    double minLon = allPoints.get(0).getLongitude();
                    double maxLon = allPoints.get(0).getLongitude();

                    for (GeoPoint p : allPoints) {
                        minLat = Math.min(minLat, p.getLatitude());
                        maxLat = Math.max(maxLat, p.getLatitude());
                        minLon = Math.min(minLon, p.getLongitude());
                        maxLon = Math.max(maxLon, p.getLongitude());
                    }

                    BoundingBox box = new BoundingBox(maxLat + 0.001, maxLon + 0.001, minLat - 0.001, minLon - 0.001);
                    mapView.zoomToBoundingBox(box, true);
                }

                mapView.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ================= POPUP BOTTOM SCREEN =================
    private void showPopupBottom(ReportItem report) {
        popupContainer.removeAllViews();

        View popupCard = LayoutInflater.from(getContext())
                .inflate(R.layout.item_mini_card_popup, popupContainer, false);

        ImageView ivCardPhoto = popupCard.findViewById(R.id.imgItem);
        TextView tvName = popupCard.findViewById(R.id.tvItemName);
        TextView tvLocation = popupCard.findViewById(R.id.tvLocation);
        TextView tvContact = popupCard.findViewById(R.id.tvContact); // new
        TextView tvDate = popupCard.findViewById(R.id.tvDate);

        tvName.setText(report.getItemName() != null ? report.getItemName() : "-");
        tvLocation.setText(report.getLocation() != null ? report.getLocation() : "-");
        tvContact.setText(report.getContact() != null ? report.getContact() : "-");
        tvDate.setText(report.getDate() != null ? report.getDate() : "-");

        // Image
        if (report.getImageBase64() != null && !report.getImageBase64().isEmpty()) {
            try {
                byte[] decoded = Base64.decode(report.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                ivCardPhoto.setImageBitmap(bitmap);

                ivCardPhoto.setOnClickListener(v -> {
                    ImagePreviewFragment preview = ImagePreviewFragment.newInstance(report.getImageBase64());
                    preview.show(getParentFragmentManager(), "preview");
                });
            } catch (Exception e) {
                e.printStackTrace();
                ivCardPhoto.setImageResource(R.drawable.bg_popup_card);
            }
        } else {
            ivCardPhoto.setImageResource(R.drawable.bg_popup_card);
        }

        // Bottom-center
        popupCard.post(() -> {
            int parentWidth = popupContainer.getWidth();
            int parentHeight = popupContainer.getHeight();
            popupCard.setX((parentWidth - popupCard.getWidth()) / 2f);
            popupCard.setY(parentHeight - popupCard.getHeight() - 50); // 50px above bottom
        });

        popupContainer.addView(popupCard);
    }
}
