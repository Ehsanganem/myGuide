package com.example.myguidefirebase;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CertificationDetailActivity extends AppCompatActivity {

    private TextView textViewUserName, textViewUserEmail, textViewUserRequest;
    private ImageView imageViewIdPhoto;
    private WebView webViewCertification;
    private EditText editTextAdminComments;
    private String certificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certification_detail);

        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewUserRequest = findViewById(R.id.textViewUserRequest);
        imageViewIdPhoto = findViewById(R.id.imageViewIdPhoto);
        webViewCertification = findViewById(R.id.webViewCertification);
        editTextAdminComments = findViewById(R.id.editTextAdminComments);

        Button buttonApprove = findViewById(R.id.buttonApprove);
        Button buttonDeny = findViewById(R.id.buttonDeny);

        certificationId = getIntent().getStringExtra("certificationId");

        if (certificationId != null) {
            loadCertificationDetails(certificationId);
        } else {
            Toast.makeText(this, "Invalid certification ID", Toast.LENGTH_SHORT).show();
            finish();  // Close the activity if the certification ID is not provided
        }

        buttonApprove.setOnClickListener(v -> updateCertificationStatus("approved"));
        buttonDeny.setOnClickListener(v -> updateCertificationStatus("denied"));
    }

    private void loadCertificationDetails(String certificationId) {
        FirebaseFirestore.getInstance().collection("certifications")
                .document(certificationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Certification certification = documentSnapshot.toObject(Certification.class);

                        if (certification != null) {
                            textViewUserName.setText(certification.getUserName());
                            textViewUserEmail.setText(certification.getUserEmail());
                            textViewUserRequest.setText(certification.getUserRequest());

                            // Load ID photo with Glide
                            Glide.with(this)
                                    .load(certification.getIdPhotoUrl())
                                    .placeholder(R.drawable.placeholder)  // Add a placeholder image
                                    .error(R.drawable.error_image)  // Handle error case
                                    .into(imageViewIdPhoto);

                            // Load certification PDF in WebView
                            if (certification.getCertificationUrl() != null && !certification.getCertificationUrl().isEmpty()) {
                                webViewCertification.getSettings().setJavaScriptEnabled(true);
                                webViewCertification.loadUrl("https://docs.google.com/gview?embedded=true&url=" + certification.getCertificationUrl());
                            } else {
                                Toast.makeText(this, "Certification document not available.", Toast.LENGTH_SHORT).show();
                            }

                            if (certification.getAdminComments() != null) {
                                editTextAdminComments.setText(certification.getAdminComments());
                            }
                        } else {
                            Toast.makeText(this, "Failed to load certification details.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Certification does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading certification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateCertificationStatus(String status) {
        String adminComments = editTextAdminComments.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("certifications")
                .document(certificationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Certification certification = documentSnapshot.toObject(Certification.class);

                        if (certification != null) {
                            db.collection("certifications")
                                    .document(certificationId)
                                    .update("status", status, "adminComments", adminComments)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Certification " + status, Toast.LENGTH_SHORT).show();

                                        String userId = certification.getUserId();
                                        String newRole;

                                        if (status.equals("approved")) {
                                            newRole = certification.isCertified() ? "certified_guide" : "uncertified_guide";
                                        } else {
                                            newRole = "user";
                                        }

                                        if (userId != null) {
                                            db.collection("users").document(userId)
                                                    .update("role", newRole)
                                                    .addOnSuccessListener(unused -> {
                                                        Toast.makeText(CertificationDetailActivity.this, "User role updated to " + newRole, Toast.LENGTH_SHORT).show();

                                                        // Send notification
                                                        sendNotificationToUser(userId, status, adminComments);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(CertificationDetailActivity.this, "Failed to update user role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        } else {
                                            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Certification details not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Certification does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to retrieve certification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendNotificationToUser(String userId, String status, String adminComments) {
        String title = status.equals("approved") ? "Guide Application Approved" : "Guide Application Denied";
        String message = "Your application to become a guide has been " + status + ". Admin Comments: " + adminComments;

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("isRead", false);

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(userId)
                .collection("userNotifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // Notification successfully added to Firestore
                })
                .addOnFailureListener(e -> Toast.makeText(CertificationDetailActivity.this, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
