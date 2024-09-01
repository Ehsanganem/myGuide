package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class BookingActivity extends AppCompatActivity {

    private DatePicker datePickerStart, datePickerEnd;
    private TextView textViewTotalCost, textViewGuideName;
    private Button buttonSubmitBooking;
    private User selectedGuide;
    private double totalCost = 0.0;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        datePickerStart = findViewById(R.id.datePickerStart);
        datePickerEnd = findViewById(R.id.datePickerEnd);
        textViewTotalCost = findViewById(R.id.textViewTotalCost);
        textViewGuideName = findViewById(R.id.textViewGuideName);
        buttonSubmitBooking = findViewById(R.id.buttonSubmitBooking);

        db = FirebaseFirestore.getInstance();

        // Get the selected guide from the intent
        selectedGuide = (User) getIntent().getSerializableExtra("selectedGuide");

        // Display the guide's name
        textViewGuideName.setText(selectedGuide.getName());

        // Calculate total cost when dates are selected
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePickerEnd.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> calculateTotalCost());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePickerStart.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> calculateTotalCost());
        }

        buttonSubmitBooking.setOnClickListener(v -> {
            Date startDate = getDateFromDatePicker(datePickerStart);
            Date endDate = getDateFromDatePicker(datePickerEnd);

            // Validate the date selection
            if (endDate.before(startDate)) {
                Toast.makeText(BookingActivity.this, "End date cannot be before start date.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check guide's availability before saving the booking
            checkAvailabilityAndSaveBooking(startDate, endDate, totalCost);
        });
    }

    private Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    private void calculateTotalCost() {
        Date startDate = getDateFromDatePicker(datePickerStart);
        Date endDate = getDateFromDatePicker(datePickerEnd);

        if (endDate.before(startDate)) {
            textViewTotalCost.setText("Invalid date selection");
            totalCost = 0;
            return;
        }

        long days = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;

        // Remove any non-numeric characters from the pricePerDay string
        String pricePerDay = selectedGuide.getPricePerDay().replaceAll("[^\\d.]", "");

        try {
            totalCost = days * Double.parseDouble(pricePerDay);
            textViewTotalCost.setText("Total Cost: $" + totalCost);
        } catch (NumberFormatException e) {
            textViewTotalCost.setText("Error calculating cost");
            e.printStackTrace();
        }
    }

    private void checkAvailabilityAndSaveBooking(Date startDate, Date endDate, double totalCost) {
        db.collection("bookings")
                .whereEqualTo("guideId", selectedGuide.getUserId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isAvailable = true;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Booking booking = document.toObject(Booking.class);
                            if (datesOverlap(startDate, endDate, booking.getStartDate(), booking.getEndDate())) {
                                isAvailable = false;
                                break;
                            }
                        }

                        if (isAvailable) {
                            saveBookingToFirestore(startDate, endDate, totalCost);
                        } else {
                            Toast.makeText(BookingActivity.this, "Selected dates are not available for this guide.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(BookingActivity.this, "Failed to check availability: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean datesOverlap(Date start1, Date end1, Date start2, Date end2) {
        return start1.before(end2) && end1.after(start2);
    }

    private void saveBookingToFirestore(Date startDate, Date endDate, double totalCost) {
        String bookingId = UUID.randomUUID().toString();
        String touristId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String guideId = selectedGuide.getUserId();
        String confirmationNumber = generateConfirmationNumber(); // Generate confirmation number

        Booking booking = new Booking(
                bookingId,
                touristId,
                guideId,
                selectedGuide.getName(),
                startDate,
                endDate,
                totalCost,
                "pending",
                confirmationNumber // Set the confirmation number here
        );

        db.collection("bookings")
                .document(bookingId)
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BookingActivity.this, "Booking submitted successfully!", Toast.LENGTH_SHORT).show();

                    // Save the guide's availability
                    saveAvailabilityToFirestore(guideId, startDate, endDate);

                    // Navigate to Booking Confirmation Activity
                    Intent intent = new Intent(BookingActivity.this, BookingConfirmationActivity.class);
                    intent.putExtra("bookingDetails", booking);
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(BookingActivity.this, "Failed to submit booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String generateConfirmationNumber() {
        // Generate a simple alphanumeric confirmation number
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void saveAvailabilityToFirestore(String guideId, Date startDate, Date endDate) {
        String availabilityId = UUID.randomUUID().toString();
        Availability availability = new Availability(guideId, startDate, endDate);

        db.collection("availability")
                .document(availabilityId)
                .set(availability)
                .addOnSuccessListener(aVoid -> Toast.makeText(BookingActivity.this, "Availability saved successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(BookingActivity.this, "Failed to save availability: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
