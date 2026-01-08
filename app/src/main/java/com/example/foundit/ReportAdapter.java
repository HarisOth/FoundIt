package com.example.foundit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final FragmentActivity activity;
    private final List<ReportItem> reportList;
    private final String currentUserId;

    public ReportAdapter(FragmentActivity activity, List<ReportItem> reportList, String currentUserId) {
        this.activity = activity;
        this.reportList = reportList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.item_report_card, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {

        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        ReportItem item = reportList.get(pos);

        // ===== BASIC INFO =====
        holder.tvItemName.setText(item.getItemName());
        holder.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "-");
        holder.tvLocation.setText(item.getLocation() != null ? item.getLocation() : "-");
        holder.tvCategory.setText(item.getCategory() != null ? item.getCategory().toUpperCase() : "-");
        holder.tvDate.setText(item.getDate() != null ? item.getDate() : "-");

        // ===== STATUS =====
        String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "OPEN";
        holder.tvStatus.setText(status);
        switch (status) {
            case "OPEN":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_open);
                break;
            case "CLAIMED":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_claimed);
                break;
            case "RESOLVED":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_resolved);
                break;
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_open);
        }

        // ===== IMAGE =====
        if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
            try {
                byte[] decoded = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                holder.imgItem.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imgItem.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.imgItem.setImageResource(R.drawable.ic_image_placeholder);
        }

        // ===== IMAGE CLICK PREVIEW =====
        holder.imgItem.setOnClickListener(v -> {
            if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
                ImagePreviewFragment preview = ImagePreviewFragment.newInstance(item.getImageBase64());
                preview.show(activity.getSupportFragmentManager(), "preview");
            } else {
                Toast.makeText(activity, "No image to preview", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== CONTACT BUTTON =====
        holder.tvContactButton.setOnClickListener(v -> {
            String phone = item.getContact() != null ? item.getContact().replaceAll("[^0-9+]", "") : "";
            if (!phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:" + phone));
                activity.startActivity(intent);
            }
        });

        // ===== LIKE SYSTEM =====
        DatabaseReference likesRef = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("reports")
                .child(item.getId())
                .child("likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                holder.tvLikeCount.setText(count + " like" + (count != 1 ? "s" : ""));
                if (snapshot.hasChild(currentUserId)) {
                    holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                    holder.btnLike.setBackgroundResource(R.drawable.bg_like_button_active);
                } else {
                    holder.btnLike.setImageResource(R.drawable.ic_heart_outline);
                    holder.btnLike.setBackgroundResource(R.drawable.bg_like_button);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // ===== LIKE CLICK & NOTIFICATION =====
        if (!item.getOwnerId().equals(currentUserId)) {
            holder.btnLike.setVisibility(View.VISIBLE);
            holder.btnLike.setOnClickListener(v -> {
                likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean alreadyLiked = snapshot.hasChild(currentUserId);
                        if (alreadyLiked) {
                            likesRef.child(currentUserId).removeValue();
                        } else {
                            likesRef.child(currentUserId).setValue(true);

                            // ✅ Push notification to owner
                            pushNotificationToOwner(
                                    item.getOwnerId(),
                                    "Someone liked your report \"" + item.getItemName() + "\""
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        } else {
            holder.btnLike.setVisibility(View.GONE);
        }
    }

    /* ================= NOTIFICATION ================= */
    private void pushNotificationToOwner(String ownerId, String message) {
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

        // ✅ Tulis ke Firebase supaya AlertsFragment owner nampak
        ref.setValue(item);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem, btnLike;
        TextView tvItemName, tvDescription, tvLocation, tvCategory, tvDate, tvLikeCount, tvContactButton, tvStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgItem);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvContactButton = itemView.findViewById(R.id.tvContactButton);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
