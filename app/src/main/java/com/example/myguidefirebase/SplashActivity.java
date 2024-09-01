package com.example.myguidefirebase;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {
    private Button btnLogin;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        // Initialize UI components
        initializeUI();

        // Setup notifications
        setupNotifications();

        // Check the user session
        checkUserSession();
    }

    private void setupNotifications() {
        // Request notification permission for Android 13+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Create a notification channel if necessary (for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel_id";
            String channelName = "Default Channel";
            String channelDescription = "Channel for default notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkUserSession() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (!currentUser.isEmailVerified()) {
                // Redirect to VerificationActivity if email is not verified
                Intent verificationIntent = new Intent(SplashActivity.this, VerificationActivity.class);
                startActivity(verificationIntent);
                finish();
            } else {
                // Check if profile is complete
                FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Boolean isProfileComplete = task.getResult().getBoolean("isProfileComplete");
                                if (isProfileComplete != null && isProfileComplete) {
                                    // Profile is complete, redirect to MainActivity
                                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                } else {
                                    // Profile is not complete, redirect to UserProfileSetupActivity
                                    Intent profileSetupIntent = new Intent(SplashActivity.this, UserProfileSetupActivity.class);
                                    startActivity(profileSetupIntent);
                                    finish();
                                }
                            } else {
                                // Handle errors (optional)
                                Toast.makeText(SplashActivity.this, "Failed to check profile status.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            // If the user is not signed in, continue with the current layout
            setContentView(R.layout.splash_activity);
            initializeUI();
        }
    }

    private void initializeUI() {
        btnLogin = findViewById(R.id.btnlogin);
        registerBtn = findViewById(R.id.registerbtn);

        btnLogin.setOnClickListener(v -> {
            Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        // Handle register button click
        registerBtn.setOnClickListener(v -> {
            Intent registerIntent = new Intent(SplashActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });
    }
}
