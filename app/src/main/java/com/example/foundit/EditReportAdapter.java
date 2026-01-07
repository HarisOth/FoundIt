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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EditReportAdapter extends RecyclerView.Adapter<EditReportAdapter.ReportViewHolder> {

    private final AppCompatActivity activity;
    private final List<ReportItem> reportList;
    private final OnReportClickListener listener;

    public interface OnReportClickListener {
        void onReportClick(ReportItem report);
    }

    public EditReportAdapter(AppCompatActivity activity, List<ReportItem> reportList, OnReportClickListener listener) {
        this.activity = activity;
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_report_card_edit, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {

        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        ReportItem item = reportList.get(pos);

        // ===== BASIC INFO =====
        holder.tvItemName.setText(item.getItemName() != null ? item.getItemName() : "No Name");
        holder.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "No Description");
        holder.tvLocation.setText(item.getLocation() != null ? item.getLocation() : "-");
        holder.tvCategory.setText(item.getCategory() != null ? item.getCategory().toUpperCase() : "-");

        // ===== DATE =====
        holder.tvDate.setText(item.getDate() != null ? item.getDate() : "-");

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
                break;
        }

        // ===== IMAGE CLICK =====
        holder.imgItem.setOnClickListener(v -> {
            if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
                ImagePreviewFragment preview = ImagePreviewFragment.newInstance(item.getImageBase64());
                preview.show(activity.getSupportFragmentManager(), "preview");
            } else {
                Toast.makeText(activity, "No image to preview", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== CARD CLICK =====
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReportClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        ImageView imgItem;
        TextView tvItemName, tvDescription, tvLocation, tvCategory, tvDate, tvStatus;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgItem);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
