package com.example.myguidefirebase;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BookingConfirmationActivity extends AppCompatActivity {

    private TextView textViewGuideName, textViewGuideRole, textViewBookingDates, textViewTotalCost;
    private Button buttonConfirmBooking, buttonCancelBooking;
    private Booking booking;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        db = FirebaseFirestore.getInstance();

        textViewGuideName = findViewById(R.id.textViewGuideName);
        textViewGuideRole = findViewById(R.id.textViewGuideRole);
        textViewBookingDates = findViewById(R.id.textViewBookingDates);
        textViewTotalCost = findViewById(R.id.textViewTotalCost);
        buttonConfirmBooking = findViewById(R.id.buttonConfirmBooking);
        buttonCancelBooking = findViewById(R.id.buttonCancelBooking);

        booking = (Booking) getIntent().getSerializableExtra("bookingDetails");

        if (booking != null) {
            textViewGuideName.setText(booking.getGuideName());
            textViewGuideRole.setText("Role: " + booking.getGuideName());
            textViewBookingDates.setText("From: " + booking.getStartDate() + " To: " + booking.getEndDate());
            textViewTotalCost.setText("Total Cost: $" + booking.getTotalCost());
        }

        buttonConfirmBooking.setOnClickListener(v -> confirmBooking());

        buttonCancelBooking.setOnClickListener(v -> cancelBooking());
    }

    private void confirmBooking() {
        booking.setStatus("confirmed");

        db.collection("bookings")
                .document(booking.getBookingId())
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    sendNotificationToGuideAndTourist("Booking Confirmed", "Your booking with " + booking.getGuideName() + " from " + booking.getStartDate() + " to " + booking.getEndDate() + " has been confirmed.");
                    Toast.makeText(BookingConfirmationActivity.this, "Booking confirmed!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to confirm booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelBooking() {
        booking.setStatus("canceled");

        db.collection("bookings")
                .document(booking.getBookingId())
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    sendNotificationToGuideAndTourist("Booking Canceled", "Your booking with " + booking.getGuideName() + " from " + booking.getStartDate() + " to " + booking.getEndDate() + " has been canceled.");
                    Toast.makeText(BookingConfirmationActivity.this, "Booking canceled.", Toast.LENGTH_SHORT).show();
                    removeAvailability();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeAvailability() {
        db.collection("availability")
                .whereEqualTo("guideId", booking.getGuideId())
                .whereEqualTo("startDate", booking.getStartDate())
                .whereEqualTo("endDate", booking.getEndDate())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete().addOnSuccessListener(aVoid -> {
                            Toast.makeText(BookingConfirmationActivity.this, "Availability removed successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to remove availability: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotificationToGuideAndTourist(String title, String message) {
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
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to add notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close the activity and go back to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
