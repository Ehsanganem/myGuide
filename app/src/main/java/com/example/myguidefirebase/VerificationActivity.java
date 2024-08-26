package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button buttonCheckVerification, buttonResendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mAuth = FirebaseAuth.getInstance();
        buttonCheckVerification = findViewById(R.id.buttonCheckVerification);
        buttonResendEmail = findViewById(R.id.buttonResendEmail);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        buttonCheckVerification.setOnClickListener(v -> checkEmailVerification());
        buttonResendEmail.setOnClickListener(v -> resendVerificationEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_verification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            // Handle logout action
            mAuth.signOut();
            Intent intent = new Intent(VerificationActivity.this, SplashActivity.class);
            startActivity(intent);
            finish(); // Close the activity after logout
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        Toast.makeText(this, "Email verified. Proceeding to profile setup.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(VerificationActivity.this, UserProfileSetupActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Email not verified. Please verify your email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("VerificationActivity", "Failed to reload user.", task.getException());
                    Toast.makeText(this, "Failed to check verification status.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("VerificationActivity", "Failed to send verification email.", task.getException());
                    Toast.makeText(this, "Failed to resend verification email.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
