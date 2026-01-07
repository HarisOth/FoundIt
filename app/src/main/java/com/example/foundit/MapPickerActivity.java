package com.example.foundit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapPickerActivity extends AppCompatActivity {

    private MapView mapView;
    private Marker marker;
    private TextView tvLatLng;
    private Button btnConfirm, btnMyLocation;

    private double selectedLat = 3.1390;   // default coordinate
    private double selectedLng = 101.6869;

    private static final int REQUEST_LOCATION_PERMISSION = 200;
    private MyLocationNewOverlay locationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_picker);

        // Tukar warna status bar supaya sama macam background splash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFDE7")); // warna cerah
        }

        // Tukar icon status bar jadi gelap supaya nampak pada background cerah
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        mapView = findViewById(R.id.mapView);
        tvLatLng = findViewById(R.id.tvLatLng);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnMyLocation = findViewById(R.id.btnMyLocation);

        // -------- Map Settings --------
        mapView.setBuiltInZoomControls(true); // show zoom buttons
        mapView.setMultiTouchControls(true);  // allow pinch zoom
        mapView.getController().setZoom(15.0);

        GeoPoint startPoint = new GeoPoint(selectedLat, selectedLng);
        mapView.getController().setCenter(startPoint);

        // -------- Marker --------
        marker = new Marker(mapView);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);

        tvLatLng.setText("Lat: " + selectedLat + " | Lng: " + selectedLng);

        // -------- Tap to move marker --------
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                GeoPoint tappedPoint = (GeoPoint)
                        mapView.getProjection().fromPixels(
                                (int) event.getX(), (int) event.getY());
                selectedLat = tappedPoint.getLatitude();
                selectedLng = tappedPoint.getLongitude();

                marker.setPosition(tappedPoint);
                mapView.invalidate();

                tvLatLng.setText("Lat: " + selectedLat + " | Lng: " + selectedLng);
            }
            return false; // important: return false supaya pinch zoom masih boleh
        });

        // -------- Confirm Button --------
        btnConfirm.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("lat", selectedLat);
            result.putExtra("lng", selectedLng);
            setResult(Activity.RESULT_OK, result);
            finish();
        });

        // -------- My Location --------
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        btnMyLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                moveMarkerToMyLocation();
            }
        });
    }

    private void moveMarkerToMyLocation() {
        if (locationOverlay.getMyLocation() != null) {
            GeoPoint myLoc = locationOverlay.getMyLocation();

            selectedLat = myLoc.getLatitude();
            selectedLng = myLoc.getLongitude();

            // update marker
            marker.setPosition(myLoc);
            mapView.getController().setCenter(myLoc);
            mapView.invalidate();

            tvLatLng.setText("Lat: " + selectedLat + " | Lng: " + selectedLng);
        } else {
            Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
        }
    }

    // -------- Handle permission result --------
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moveMarkerToMyLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
