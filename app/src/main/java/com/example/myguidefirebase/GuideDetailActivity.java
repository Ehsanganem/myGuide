package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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
    private Button buttonRequestBooking;
    private User selectedGuide;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_detail);

        imageViewProfilePic = findViewById(R.id.imageViewProfilePic);
        textViewName = findViewById(R.id.textViewName);
        textViewRole = findViewById(R.id.textViewRole);
        textViewBio = findViewById(R.id.textViewBio);
        textViewServices = findViewById(R.id.textViewServices);
        textViewPricePerDay = findViewById(R.id.textViewPricePerDay);
        textViewBookedDates = findViewById(R.id.textViewBookedDates);
        buttonRequestBooking = findViewById(R.id.buttonRequestBooking);

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
}
