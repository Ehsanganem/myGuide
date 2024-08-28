package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BookingConfirmationActivity extends AppCompatActivity {

    private TextView textViewGuideName, textViewGuideRole, textViewBookingDates, textViewTotalCost;
    private Button buttonConfirmBooking, buttonCancelBooking;
    private Booking booking;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        textViewGuideName = findViewById(R.id.textViewGuideName);
        textViewGuideRole = findViewById(R.id.textViewGuideRole);
        textViewBookingDates = findViewById(R.id.textViewBookingDates);
        textViewTotalCost = findViewById(R.id.textViewTotalCost);
        buttonConfirmBooking = findViewById(R.id.buttonConfirmBooking);
        buttonCancelBooking = findViewById(R.id.buttonCancelBooking);

        // Retrieve the Booking object passed from the previous activity
        booking = (Booking) getIntent().getSerializableExtra("bookingDetails");

        if (booking != null) {
            // Populate the UI with booking details
            textViewGuideName.setText(booking.getGuideName());
            textViewGuideRole.setText("Role: " + booking.getGuideName());
            textViewBookingDates.setText("From: " + booking.getStartDate() + " To: " + booking.getEndDate());
            textViewTotalCost.setText("Total Cost: $" + booking.getTotalCost());
        }

        // Handle confirm booking button click
        buttonConfirmBooking.setOnClickListener(v -> confirmBooking());

        // Handle cancel button click
        buttonCancelBooking.setOnClickListener(v -> cancelBooking());
    }

    private void confirmBooking() {
        // Update booking status to confirmed
        booking.setStatus("confirmed");

        // Save the updated booking to Firestore
        db.collection("bookings")
                .document(booking.getBookingId())
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Booking confirmed!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to confirm booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelBooking() {
        // Update booking status to canceled
        booking.setStatus("canceled");

        // Save the updated booking to Firestore
        db.collection("bookings")
                .document(booking.getBookingId())
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BookingConfirmationActivity.this, "Booking canceled.", Toast.LENGTH_SHORT).show();

                    // Remove the availability corresponding to the canceled booking
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
}
