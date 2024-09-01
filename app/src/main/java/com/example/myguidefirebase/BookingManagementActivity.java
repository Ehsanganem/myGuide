package com.example.myguidefirebase;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BookingManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerViewBookings;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private String currentUserId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_management);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList, this::cancelBooking, this); // Pass context
        recyclerViewBookings.setAdapter(bookingAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        loadBookings();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close the activity and go back to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void loadBookings() {
        db.collection("bookings")
                .whereEqualTo("guideId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        bookingList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Booking booking = document.toObject(Booking.class);
                            if (booking != null) {
                                String displayText = "Tourist ID: " + booking.getTouristId() + "\nGuide ID: " + booking.getGuideId();
                                Log.d("BookingManagement", displayText);
                                bookingList.add(booking);
                            }
                        }
                        bookingAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(BookingManagementActivity.this, "Failed to load bookings.", Toast.LENGTH_SHORT).show();
                        Log.e("BookingManagement", "Error loading bookings", task.getException());
                    }
                });
    }

    private void cancelBooking(Booking booking) {
        Date today = new Date();
        long diff = booking.getStartDate().getTime() - today.getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        if (daysDiff > 7) {
            booking.setStatus("canceled");

            db.collection("bookings")
                    .document(booking.getBookingId())
                    .set(booking)
                    .addOnSuccessListener(aVoid -> {
                        sendNotificationToGuideAndTourist(booking, "Booking Canceled", "Your booking with " + booking.getGuideName() + " from " + booking.getStartDate() + " to " + booking.getEndDate() + " has been canceled.");
                        Toast.makeText(BookingManagementActivity.this, "Booking canceled successfully", Toast.LENGTH_SHORT).show();
                        bookingAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(BookingManagementActivity.this, "Failed to cancel booking", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Cannot cancel booking within a week of the date", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationToGuideAndTourist(Booking booking, String title, String message) {
        MyFirebaseMessagingService messagingService = new MyFirebaseMessagingService();
        messagingService.sendNotification(this, title, message);

        // Update Firebase Firestore with the notification for both guide and tourist
        addNotificationToFirebase(booking.getTouristId(), title, message);
        addNotificationToFirebase(booking.getGuideId(), title, message);
    }

    private void addNotificationToFirebase(String userId, String title, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("isRead", false);
        notification.put("timestamp", new Date()); // Add the timestamp

        db.collection("notifications")
                .document(userId)
                .collection("userNotifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // Notification successfully added to Firestore
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingManagementActivity.this, "Failed to add notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
