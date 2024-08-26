package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        initializeUI();
        checkUserSession();
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
