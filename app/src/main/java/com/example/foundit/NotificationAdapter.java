package com.example.foundit;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final List<NotificationItem> notificationList;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        holder.tvMessage.setText(item.getMessage());
        // Bold jika unread
        holder.tvMessage.setTypeface(null, item.isRead() ? Typeface.NORMAL : Typeface.BOLD);

        // Format timestamp
        String timeText = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(new Date(item.getTimestamp()));
        holder.tvTime.setText(timeText);

        // Tap → set read = true
        holder.cardView.setOnClickListener(v -> {
            if (!item.isRead() && item.getId() != null) {
                FirebaseDatabase.getInstance(
                                "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference("notifications")
                        .child(item.getId()) // ini ID dari Firebase key
                        .child("read")
                        .setValue(true);

                item.setRead(true);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        CardView cardView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
