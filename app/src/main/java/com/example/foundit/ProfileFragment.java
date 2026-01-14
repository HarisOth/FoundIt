package com.example.foundit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView tvUsername, tvContact;

    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(Color.parseColor("#FFFDE7"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        // --- Bind views ---
        imgProfile = view.findViewById(R.id.imgProfile);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvContact = view.findViewById(R.id.tvContact);

        // --- Button functionality ---
        view.findViewById(R.id.cardEdit).setOnClickListener(v ->
                startActivity(new Intent(getContext(), EditProfileActivity.class)));

        view.findViewById(R.id.cardNotifications).setOnClickListener(v ->
                startActivity(new Intent(getContext(), NotificationsActivity.class)));

        view.findViewById(R.id.cardAbout).setOnClickListener(v ->
                startActivity(new Intent(getContext(), AboutUsActivity.class)));

        view.findViewById(R.id.cardSignOut).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT).show();

            if (getContext() != null) {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return view;

        userRef = FirebaseDatabase.getInstance(
                "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("Users").child(currentUser.getUid());

        loadUserProfile();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (userRef == null) return;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String username = snapshot.child("username").getValue(String.class);
                String contact = snapshot.child("contact").getValue(String.class);
                String profileImage = snapshot.child("profileImage").getValue(String.class);

                if (username != null) tvUsername.setText(username);
                if (contact != null) tvContact.setText(contact);

                if (profileImage != null && !profileImage.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(profileImage, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imgProfile.setImageBitmap(getCircularBitmap(bmp));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to load profile image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== helper make circular bitmap =====
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);

        return output;
    }
}
