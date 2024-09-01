package com.example.myguidefirebase;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

        notificationAdapter = new NotificationAdapter(new ArrayList<>());
        recyclerViewNotifications.setAdapter(notificationAdapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadNotifications();

        // Set up the Clear Notifications TextView
        TextView textViewClearNotifications = findViewById(R.id.textViewClearNotifications);
        textViewClearNotifications.setOnClickListener(v -> {
            Log.d("NotificationActivity", "Clear Notifications clicked");
            clearNotifications();
        });
    }

    private void loadNotifications() {
        db.collection("notifications")
                .document(userId)
                .collection("userNotifications")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Notification> notifications = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Notification notification = document.toObject(Notification.class);
                            notifications.add(notification);

                            // Mark the notification as read
                            document.getReference().update("isRead", true);
                        }
                        notificationAdapter.updateNotifications(notifications);
                    } else {
                        Log.e("NotificationActivity", "Error fetching notifications", task.getException());
                    }
                });
    }

    private void clearNotifications() {
        db.collection("notifications")
                .document(userId)
                .collection("userNotifications")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("NotificationActivity", "Notification deleted"))
                                    .addOnFailureListener(e -> Log.e("NotificationActivity", "Error deleting notification", e));
                        }
                        notificationAdapter.updateNotifications(new ArrayList<>());
                        Toast.makeText(NotificationActivity.this, "Notifications cleared", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("NotificationActivity", "Error clearing notifications", task.getException());
                    }
                });
    }
}
