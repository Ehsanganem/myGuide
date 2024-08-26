package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardPendingCertifications, cardUserRoles, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize the CardViews
        cardPendingCertifications = findViewById(R.id.cardPendingCertifications);
        cardUserRoles = findViewById(R.id.cardUserRoles);
        cardLogout = findViewById(R.id.cardLogout);

        // Set onClickListeners for the cards
        cardPendingCertifications.setOnClickListener(v -> {
            // Navigate to PendingCertificationsActivity
            Intent intent = new Intent(AdminDashboardActivity.this, PendingCertificationsActivity.class);
            startActivity(intent);
        });

        cardUserRoles.setOnClickListener(v -> {
            // Navigate to ManageUserRolesActivity
            Intent intent = new Intent(AdminDashboardActivity.this, ManageUserRolesActivity.class);
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            // Handle logout
            logoutAdmin();
        });
    }

    private void logoutAdmin() {
        // Implement the logout logic
        Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        // Redirect to LoginActivity after logout
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close the AdminDashboardActivity
    }
}
