package com.example.foundit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class AlertsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ArrayList<NotificationItem> notificationList;
    private DatabaseReference notificationsRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        recyclerView = view.findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        adapter = new NotificationAdapter(getContext(), notificationList);
        recyclerView.setAdapter(adapter);

        notificationsRef = FirebaseDatabase.getInstance(
                        "https://foundit-24436-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("notifications")
                .child(currentUserId);

        notificationsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                NotificationItem item = snapshot.getValue(NotificationItem.class);
                if (item != null) {
                    item.setId(snapshot.getKey());
                    boolean exists = false;
                    for (NotificationItem n : notificationList) {
                        if (n.getId() != null && n.getId().equals(item.getId())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        notificationList.add(item);
                        sortNotifications();
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                NotificationItem updated = snapshot.getValue(NotificationItem.class);
                if (updated != null) {
                    updated.setId(snapshot.getKey());
                    for (int i = 0; i < notificationList.size(); i++) {
                        if (notificationList.get(i).getId().equals(updated.getId())) {
                            notificationList.set(i, updated);
                            sortNotifications();
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AlertsFragment","Failed to load notifications: "+error.getMessage());
                Toast.makeText(getContext(),"Failed to load notifications",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void sortNotifications() {
        Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
    }
}
