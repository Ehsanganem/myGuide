package com.example.myguidefirebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView searchGuides, imageViewEnlistAsguide; // Added ImageView for booking management
    private TextView notificationBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize ImageViews
        searchGuides = findViewById(R.id.searchGuides);
        imageViewEnlistAsguide = findViewById(R.id.imageViewEnlistAsguide); // Added initialization

        // Set OnClickListener for Search Guides ImageView
        searchGuides.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchGuidesActivity.class);
            startActivity(intent);
        });

        // Set OnClickListener for Manage Bookings ImageView
        imageViewEnlistAsguide.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EnlistAsGuideActivity.class); // Assuming BookingManagementActivity exists
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Locate the notification item
        final MenuItem notificationItem = menu.findItem(R.id.action_notifications);

        // Inflate the custom action view and set it to the MenuItem
        View actionView = getLayoutInflater().inflate(R.layout.layout_notification_badge, null);
        notificationItem.setActionView(actionView);

        // Locate the notification badge TextView within the custom view
        notificationBadge = actionView.findViewById(R.id.notificationBadge);

        // Update the notification count
        updateNotificationCount();

        // Handle notification icon click
        actionView.setOnClickListener(v -> {
            // Reset the badge count
            if (notificationBadge != null) {
                notificationBadge.setVisibility(View.GONE);
            }

            // Open the NotificationActivity
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        return true;
    }

    private void updateNotificationCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(userId)
                .collection("userNotifications")
                .whereEqualTo("isRead", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int unreadCount = task.getResult().size();
                        if (unreadCount > 0) {
                            if (notificationBadge != null) {
                                notificationBadge.setText(String.valueOf(unreadCount));
                                notificationBadge.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (notificationBadge != null) {
                                notificationBadge.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finish MainActivity
            return true;
        }

        if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }

        if (item.getItemId() == R.id.action_manage_bookings) {
            Intent intent = new Intent(MainActivity.this, BookingManagementActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
