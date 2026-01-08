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
import android.provider.MediaStore;
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
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ReportFragment extends Fragment {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_MAP_PICK = 102;

    // Form fields
    private TextInputEditText etItemName, etDescription, etLocation, etContact, etDate;
    private RadioGroup rgCategory;
    private RadioButton rbLost, rbFound;
    private MaterialButton btnSubmit, btnCapture, btnGallery, btnPickLocation;
    private ImageView imageView;
    private TextView tvLatLng;

    // Map preview
    private MapView mapPreview;
    private Marker previewMarker;

    // Firebase
    private DatabaseReference databaseReports;
    private String currentUserId;

    // Image
    private String imageBase64 = "";

    // Coordinates
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(Color.parseColor("#FFFDE7"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        // FIND VIEWS
        etItemName = view.findViewById(R.id.etItemName);
        etDescription = view.findViewById(R.id.etDescription);
        etLocation = view.findViewById(R.id.etLocation);
        etContact = view.findViewById(R.id.etContact);
        etDate = view.findViewById(R.id.etDate);

        rgCategory = view.findViewById(R.id.rgCategory);
        rbLost = view.findViewById(R.id.rbLost);
        rbFound = view.findViewById(R.id.rbFound);

        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnCapture = view.findViewById(R.id.btnCapture);
        btnGallery = view.findViewById(R.id.btnGallery);
        btnPickLocation = view.findViewById(R.id.btnPickLocation);
        imageView = view.findViewById(R.id.imageView);
        tvLatLng = view.findViewById(R.id.tvLatLng);
        mapPreview = view.findViewById(R.id.mapPreview);

        // Firebase setup
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";

        databaseReports = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("reports");

        // Default date kosong, tapi masih boleh pilih tarikh
        etDate.setText("");
        etDate.setHint("Pick a date");

        // OSMDroid map preview setup
        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());
        mapPreview.setBuiltInZoomControls(true);
        mapPreview.setMultiTouchControls(true);
        mapPreview.getController().setZoom(15.0);

        // Default marker
        previewMarker = new Marker(mapPreview);
        previewMarker.setPosition(new GeoPoint(latitude, longitude));
        previewMarker.setTitle("Selected Location");
        mapPreview.getOverlays().add(previewMarker);
        mapPreview.getController().setCenter(new GeoPoint(latitude, longitude));

        // CLICK LISTENERS
        etDate.setOnClickListener(v -> showDatePicker());
        btnCapture.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnPickLocation.setOnClickListener(v -> openMapPicker());
        btnSubmit.setOnClickListener(v -> submitReport());

        // ===== IMAGE PREVIEW FUNCTION =====
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

    /* ================= CAMERA / GALLERY ================= */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    /* ================= DATE PICKER ================= */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(),
                (view, year, month, day) ->
                        etDate.setText(day + " " + new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime()) + " " + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /* ================= MAP PICKER ================= */
    private void openMapPicker() {
        Intent intent = new Intent(getContext(), MapPickerActivity.class);
        startActivityForResult(intent, REQUEST_MAP_PICK);
    }

    /* ================= SUBMIT REPORT ================= */
    private void submitReport() {
        String itemName = etItemName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        String category = rbLost.isChecked() ? "Lost" :
                rbFound.isChecked() ? "Found" : "";

        if (itemName.isEmpty() || description.isEmpty() || location.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = UUID.randomUUID().toString();

        ReportItem report = new ReportItem(
                id,
                itemName,
                category,
                location,
                description,
                contact,
                imageBase64,
                currentUserId,
                date
        );

        report.setLatitude(latitude);
        report.setLongitude(longitude);
        report.setStatus("open");

        databaseReports.child(id).setValue(report)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_container, new HomeFragment())
                            .commit();


                    // Notification
                    pushNotificationForSubmit(currentUserId, "You have successfully submitted a report: \"" + itemName + "\"");

                    clearForm();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show());
    }

    /* ================= SYSTEM NOTIFICATION UNTUK SUBMIT ================= */
    private void pushNotificationForSubmit(String ownerId, String message) {
        // notification node untuk AlertsFragment
        DatabaseReference ref = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("notifications")
                .child(ownerId)
                .push();

        NotificationItem item = new NotificationItem(
                message,
                System.currentTimeMillis(),
                false
        );
        ref.setValue(item);

        // Tunjuk system notification
        showSystemNotification(message);
    }

    /* ================= SYSTEM NOTIFICATION UNTUK LIKE ================= */
    public void pushLikeNotification(ReportItem report, String likerName) {
        // jangan push noti ke diri sendiri
        if (report.getOwnerId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) return;

        String ownerId = report.getOwnerId();
        String message = likerName + " liked your report: \"" + report.getItemName() + "\"";

        DatabaseReference ref = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("notifications")
                .child(ownerId)
                .push();

        NotificationItem item = new NotificationItem(
                message,
                System.currentTimeMillis(),
                false
        );
        ref.setValue(item);

        // optional: tunjuk system notification kepada owner kalau dia online
        showSystemNotification(message);
    }

    /* ===== showSystemNotification() stable version ===== */
    private void showSystemNotification(String message) {
        Context ctx = getContext();
        if (ctx == null) {
            if (getActivity() != null) {
                ctx = getActivity();
            } else {
                System.out.println("Notification failed: no valid context");
                return;
            }
        }

        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        String channelId = "foundit_notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(
                        channelId,
                        "FoundIt Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
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

        Intent intent = new Intent(ctx, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int iconRes = R.drawable.ic_heart_filled;
        try {
            ctx.getResources().getDrawable(iconRes);
        } catch (Exception e) {
            iconRes = android.R.drawable.ic_dialog_info;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle("FoundIt")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());
    }

    /* ================= IMAGE TO BASE64 & HANDLE ACTIVITY RESULT ================= */
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
                InputStream is = requireActivity().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageBase64 = encodeToBase64(bitmap);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void clearForm() {
        etItemName.setText("");
        etDescription.setText("");
        etLocation.setText("");
        etContact.setText("");
        etDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()));
        rgCategory.clearCheck();
        imageView.setImageDrawable(null);
        imageBase64 = "";
        latitude = 0.0;
        longitude = 0.0;
        tvLatLng.setText("Lat: 0.0 | Lng: 0.0");
        previewMarker.setPosition(new GeoPoint(latitude, longitude));
        mapPreview.getController().setCenter(new GeoPoint(latitude, longitude));
    }
}
