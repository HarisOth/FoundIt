package com.example.foundit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditReportFragment extends Fragment {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_MAP_PICK = 102;

    private TextInputEditText etItemName, etDescription, etLocation, etContact, etDate;
    private RadioGroup rgCategory, rgStatus;
    private RadioButton rbLost, rbFound, rbOpen, rbClaimed, rbResolved;
    private MaterialButton btnUpdate, btnDelete, btnCapture, btnGallery, btnPickLocation;
    private ImageView imageView;
    private TextView tvLatLng;

    private MapView mapPreview;
    private Marker previewMarker;

    private DatabaseReference databaseReports;
    private String currentUserId;

    private String imageBase64 = "";
    private double latitude = 0.0;
    private double longitude = 0.0;

    private ReportItem report;

    public static Bundle newBundle(ReportItem report) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("report", report);
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_report, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(Color.parseColor("#FFFDE7"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        // ===== FIND VIEWS =====
        etItemName = view.findViewById(R.id.etItemName);
        etDescription = view.findViewById(R.id.etDescription);
        etLocation = view.findViewById(R.id.etLocation);
        etContact = view.findViewById(R.id.etContact);
        etDate = view.findViewById(R.id.etDate);

        rgCategory = view.findViewById(R.id.rgCategory);
        rbLost = view.findViewById(R.id.rbLost);
        rbFound = view.findViewById(R.id.rbFound);

        rgStatus = view.findViewById(R.id.rgStatus);
        rbOpen = view.findViewById(R.id.rbOpen);
        rbClaimed = view.findViewById(R.id.rbClaimed);
        rbResolved = view.findViewById(R.id.rbResolved);

        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnCapture = view.findViewById(R.id.btnCapture);
        btnGallery = view.findViewById(R.id.btnGallery);
        btnPickLocation = view.findViewById(R.id.btnPickLocation);
        imageView = view.findViewById(R.id.imageView);
        tvLatLng = view.findViewById(R.id.tvLatLng);
        mapPreview = view.findViewById(R.id.mapPreview);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";

        databaseReports = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("reports");

        // ===== MAP CONFIG =====
        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());
        mapPreview.setBuiltInZoomControls(true);
        mapPreview.setMultiTouchControls(true);
        mapPreview.getController().setZoom(15.0);

        previewMarker = new Marker(mapPreview);
        mapPreview.getOverlays().add(previewMarker);

        // ===== GET REPORT FROM ARGUMENTS =====
        if (getArguments() != null) {
            report = (ReportItem) getArguments().getSerializable("report");
            if (report != null) populateFields(report);
        }

        // ===== CLICK LISTENERS =====
        etDate.setOnClickListener(v -> showDatePicker());
        btnCapture.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnPickLocation.setOnClickListener(v -> openMapPicker());
        btnUpdate.setOnClickListener(v -> updateReport());
        btnDelete.setOnClickListener(v -> deleteReport());

        imageView.setOnClickListener(v -> {
            if (!imageBase64.isEmpty()) {
                ImagePreviewFragment preview = ImagePreviewFragment.newInstance(imageBase64);
                preview.show(getParentFragmentManager(), "preview");
            } else {
                Toast.makeText(getContext(), "No image to preview", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void populateFields(ReportItem r) {
        etItemName.setText(r.getItemName());
        etDescription.setText(r.getDescription());
        etLocation.setText(r.getLocation());
        etContact.setText(r.getContact());
        etDate.setText(r.getDate());
        imageBase64 = r.getImageBase64();

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            imageView.setImageBitmap(decodeBase64(imageBase64));
        }

        latitude = r.getLatitude();
        longitude = r.getLongitude();
        tvLatLng.setText("Lat: " + latitude + " | Lng: " + longitude);
        previewMarker.setPosition(new GeoPoint(latitude, longitude));
        mapPreview.getController().setCenter(new GeoPoint(latitude, longitude));

        if ("Lost".equalsIgnoreCase(r.getCategory())) rbLost.setChecked(true);
        else if ("Found".equalsIgnoreCase(r.getCategory())) rbFound.setChecked(true);

        if ("open".equalsIgnoreCase(r.getStatus())) rbOpen.setChecked(true);
        else if ("claimed".equalsIgnoreCase(r.getStatus())) rbClaimed.setChecked(true);
        else if ("resolved".equalsIgnoreCase(r.getStatus())) rbResolved.setChecked(true);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(),
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    String dateStr = day + " " + new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime()) + " " + year;
                    etDate.setText(dateStr);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void openCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void openMapPicker() {
        Intent intent = new Intent(getContext(), MapPickerActivity.class);
        startActivityForResult(intent, REQUEST_MAP_PICK);
    }

    private void updateReport() {
        if (report == null) return;

        String itemName = etItemName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        String category = rbLost.isChecked() ? "Lost" :
                rbFound.isChecked() ? "Found" : "";

        String status = rbOpen.isChecked() ? "open" :
                rbClaimed.isChecked() ? "claimed" :
                        rbResolved.isChecked() ? "resolved" : "open";

        if (itemName.isEmpty() || description.isEmpty() || location.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        report.setItemName(itemName);
        report.setDescription(description);
        report.setLocation(location);
        report.setContact(contact);
        report.setDate(date);
        report.setCategory(category);
        report.setStatus(status);
        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setImageBase64(imageBase64);

        databaseReports.child(report.getId()).setValue(report)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Report updated successfully", Toast.LENGTH_SHORT).show();

                    // ✅ Navigate to MainActivity + HomeFragment
                    if (getContext() != null) {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.putExtra("fragment_to_load", "home");
                        startActivity(intent);
                        requireActivity().finish();
                    }

                    pushNotificationForUpdate(currentUserId, "Your report \"" + itemName + "\" has been updated.");
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update report", Toast.LENGTH_SHORT).show());
    }

    private void deleteReport() {
        if (report == null) return;

        databaseReports.child(report.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Report deleted successfully", Toast.LENGTH_SHORT).show();

                    // ✅ Push notification for delete
                    pushNotificationForUpdate(currentUserId, "Your report \"" + report.getItemName() + "\" has been deleted.");

                    // ✅ Navigate to MainActivity + HomeFragment
                    if (getContext() != null) {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.putExtra("fragment_to_load", "home");
                        startActivity(intent);
                        requireActivity().finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete report", Toast.LENGTH_SHORT).show());
    }

    private Bitmap decodeBase64(String base64) {
        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    private String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void pushNotificationForUpdate(String ownerId, String message) {
        // Push to Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("notifications")
                .child(ownerId)
                .push();

        NotificationItem item = new NotificationItem(message, System.currentTimeMillis(), false);
        ref.setValue(item);

        // System notification
        showSystemNotification(message);
    }

    private void showSystemNotification(String message) {
        Context ctx = getContext();
        if (ctx == null && getActivity() != null) ctx = getActivity();
        if (ctx == null) return;

        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        String channelId = "foundit_notifications";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(channelId, "FoundIt Notifications",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Notifications for FoundIt app");
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build());
                manager.createNotificationChannel(channel);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0,
                new Intent(ctx, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_heart_filled)
                .setContentTitle("FoundIt")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;

        try {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageBase64 = encodeToBase64(bitmap);
                }
            } else if (requestCode == REQUEST_GALLERY) {
                Uri uri = data.getData();
                if (uri != null) {
                    InputStream is = requireActivity().getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        imageBase64 = encodeToBase64(bitmap);
                    }
                }
            } else if (requestCode == REQUEST_MAP_PICK) {
                latitude = data.getDoubleExtra("lat", 0.0);
                longitude = data.getDoubleExtra("lng", 0.0);
                tvLatLng.setText("Lat: " + latitude + " | Lng: " + longitude);
                if (previewMarker != null) {
                    previewMarker.setPosition(new GeoPoint(latitude, longitude));
                    mapPreview.getController().setCenter(new GeoPoint(latitude, longitude));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
