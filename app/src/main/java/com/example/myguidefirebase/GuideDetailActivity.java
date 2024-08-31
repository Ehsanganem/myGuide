package com.example.myguidefirebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuideDetailActivity extends AppCompatActivity {

    private ImageView imageViewProfilePic;
    private TextView textViewName, textViewRole, textViewBio, textViewServices, textViewPricePerDay, textViewBookedDates;
    private Button buttonRequestBooking, buttonViewProfile, buttonContactGuide;
    private User selectedGuide;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        imageViewProfilePic = findViewById(R.id.imageViewProfilePic);
        textViewName = findViewById(R.id.textViewName);
        textViewRole = findViewById(R.id.textViewRole);
        textViewBio = findViewById(R.id.textViewBio);
        textViewServices = findViewById(R.id.textViewServices);
        textViewPricePerDay = findViewById(R.id.textViewPricePerDay);
        textViewBookedDates = findViewById(R.id.textViewBookedDates);
        buttonRequestBooking = findViewById(R.id.buttonRequestBooking);
        buttonViewProfile = findViewById(R.id.buttonViewProfile);
        buttonContactGuide = findViewById(R.id.buttonContactGuide);

        db = FirebaseFirestore.getInstance();

        // Retrieve the User object passed from the previous activity
        selectedGuide = (User) getIntent().getSerializableExtra("selectedGuide");

        // Populate the UI with the guide's information
        textViewName.setText(selectedGuide.getName());
        textViewRole.setText("Role: " + selectedGuide.getRole());
        textViewBio.setText(selectedGuide.getBio());
        textViewServices.setText(selectedGuide.getServices().toString());
        textViewPricePerDay.setText("Price per Day: " + selectedGuide.getPricePerDay());

        // Load profile picture using Glide
        Glide.with(this)
                .load(selectedGuide.getIdPhotoUrl())
                .circleCrop()
                .into(imageViewProfilePic);

        // Fetch and display booked dates for the guide
        fetchBookedDates();

        // Handle booking request
        buttonRequestBooking.setOnClickListener(v -> {
            Intent intent = new Intent(GuideDetailActivity.this, BookingActivity.class);
            intent.putExtra("selectedGuide", selectedGuide);
            startActivity(intent);
        });

        // View Full Profile
        buttonViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(GuideDetailActivity.this, ProfileActivity.class);
            intent.putExtra("userId", selectedGuide.getUserId()); // Pass the guide's userId
            startActivity(intent);
        });

        // Contact Guide (For example, open email client)
        buttonContactGuide.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", selectedGuide.getEmail(), null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about Guide Services");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        });
    }

    private void fetchBookedDates() {
        db.collection("bookings")
                .whereEqualTo("guideId", selectedGuide.getUserId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> bookedDates = new ArrayList<>();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Booking booking = document.toObject(Booking.class);
                            String formattedStartDate = dateFormat.format(booking.getStartDate());
                            String formattedEndDate = dateFormat.format(booking.getEndDate());
                            bookedDates.add(formattedStartDate + " to " + formattedEndDate);
                        }

                        if (bookedDates.isEmpty()) {
                            textViewBookedDates.setText("No bookings yet.");
                        } else {
                            textViewBookedDates.setText("Booked Dates:\n" + String.join("\n", bookedDates));
                            textViewBookedDates.setTextColor(getResources().getColor(R.color.red)); // Set the text color to red
                        }
                    } else {
                        textViewBookedDates.setText("Failed to load bookings.");
                    }
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
