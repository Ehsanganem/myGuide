package com.example.myguidefirebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mukesh.countrypicker.CountryPicker;

import java.util.HashMap;
import java.util.Map;

public class EnlistAsGuideActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private static final int PICK_ID_PHOTO_REQUEST = 2;

    private Uri certificationUri;
    private Uri idPhotoUri;

    private Button buttonSelectCountry, buttonSaveGuideProfile, buttonSelectCertification, buttonUploadIdPhoto;
    private RadioGroup radioGroupGuideType;
    private RadioButton radioCertifiedGuide, radioUncertifiedGuide;
    private CheckBox checkBoxLocal, checkBoxOver21;
    private Spinner spinnerCriminalRecord;
    private EditText editTextUserRequest;

    private String selectedCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enlist_as_guide);

        buttonSelectCountry = findViewById(R.id.buttonSelectCountry);
        buttonSaveGuideProfile = findViewById(R.id.buttonSaveGuideProfile);
        buttonSelectCertification = findViewById(R.id.buttonSelectCertification);
        buttonUploadIdPhoto = findViewById(R.id.buttonUploadIdPhoto);
        radioGroupGuideType = findViewById(R.id.radioGroupGuideType);
        radioCertifiedGuide = findViewById(R.id.radioCertifiedGuide);
        radioUncertifiedGuide = findViewById(R.id.radioUncertifiedGuide);
        checkBoxLocal = findViewById(R.id.checkBoxLocal);
        checkBoxOver21 = findViewById(R.id.checkBoxOver21);
        spinnerCriminalRecord = findViewById(R.id.spinnerCriminalRecord);
        editTextUserRequest = findViewById(R.id.editTextUserRequest);

        buttonSelectCountry.setOnClickListener(v -> showCountryPicker());

        radioGroupGuideType.setOnCheckedChangeListener((group, checkedId) -> {
            showCertifiedGuideOptions(checkedId == R.id.radioCertifiedGuide);
        });

        buttonSelectCertification.setOnClickListener(v -> selectCertificationFile());
        buttonUploadIdPhoto.setOnClickListener(v -> selectIdPhoto());

        buttonSaveGuideProfile.setOnClickListener(v -> {
            if (validateInputs()) {
                showConfirmationDialog();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.conviction_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCriminalRecord.setAdapter(adapter);
    }

    private boolean validateInputs() {
        if (selectedCountry == null || selectedCountry.isEmpty()) {
            Toast.makeText(this, "Please select your country.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (radioCertifiedGuide.isChecked() && certificationUri == null) {
            Toast.makeText(this, "Please upload your certification document.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!checkBoxLocal.isChecked() || !checkBoxOver21.isChecked()) {
            Toast.makeText(this, "Please confirm that you meet the criteria.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (idPhotoUri == null) {
            Toast.makeText(this, "Please upload your ID photo.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (editTextUserRequest.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please provide a reason for your request.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void selectIdPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_ID_PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            certificationUri = data.getData();
            Toast.makeText(this, "Certification file selected", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == PICK_ID_PHOTO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            idPhotoUri = data.getData();
            Toast.makeText(this, "ID Photo selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCountryPicker() {
        CountryPicker picker = new CountryPicker.Builder().with(this)
                .listener(country -> {
                    selectedCountry = country.getName();
                    buttonSelectCountry.setText(selectedCountry);
                }).build();

        picker.showDialog(EnlistAsGuideActivity.this);
    }

    private void showCertifiedGuideOptions(boolean show) {
        findViewById(R.id.textViewUploadCertification).setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSelectCertification.setVisibility(show ? View.VISIBLE : View.GONE);
        checkBoxLocal.setVisibility(View.VISIBLE);
        checkBoxOver21.setVisibility(View.VISIBLE);
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Submission");

        builder.setMessage(radioCertifiedGuide.isChecked()
                ? "Are you sure you want to submit your profile as a certified guide? Your certification will be pending approval."
                : "Are you sure you want to submit your profile as an uncertified guide?");

        builder.setPositiveButton("Yes", (dialog, which) -> saveGuideProfile());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveGuideProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Failed to save profile. User is not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        Certification certification = new Certification(
                user.getUid(),
                user.getDisplayName(),
                user.getEmail(),
                radioCertifiedGuide.isChecked()
        );

        certification.setUserRequest(editTextUserRequest.getText().toString());
        certification.setStatus("pending"); // Status is always pending when initially saving

        // Additional fields like country can be set as admin comments or other fields as needed
        if (selectedCountry != null && !selectedCountry.isEmpty()) {
            certification.setAdminComments("Country: " + selectedCountry); // Optional for later admin review
        }

        if (radioCertifiedGuide.isChecked()) {
            if (certificationUri != null) {
                uploadCertificationAndSaveProfile(certification);
            } else {
                Toast.makeText(this, "Please upload your certification document.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (idPhotoUri != null) {
                uploadIdPhotoAndSaveProfile(certification);
            } else {
                Toast.makeText(this, "Please upload your ID photo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadCertificationAndSaveProfile(Certification certification) {
        if (certificationUri == null) {
            Log.e("EnlistAsGuideActivity", "Certification file URI is null.");
            Toast.makeText(this, "Certification file URI is null.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("EnlistAsGuideActivity", "Certification URI: " + certificationUri.toString());

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference certificationRef = storageRef.child("certifications/" + certification.getUserId() + ".pdf");

        certificationRef.putFile(certificationUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("EnlistAsGuideActivity", "Certification uploaded successfully.");
                    certificationRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        certification.setCertificationUrl(uri.toString());
                        Log.d("EnlistAsGuideActivity", "Certification URL: " + uri.toString());
                        uploadIdPhotoAndSaveProfile(certification);
                    }).addOnFailureListener(e -> {
                        Log.e("EnlistAsGuideActivity", "Failed to get download URL for certification: " + e.getMessage());
                        Toast.makeText(EnlistAsGuideActivity.this, "Failed to upload certification.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EnlistAsGuideActivity", "Failed to upload certification: " + e.getMessage());
                    Toast.makeText(EnlistAsGuideActivity.this, "Failed to upload certification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadIdPhotoAndSaveProfile(Certification certification) {
        if (idPhotoUri == null) {
            Log.e("EnlistAsGuideActivity", "ID photo URI is null.");
            Toast.makeText(this, "ID photo URI is null.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("EnlistAsGuideActivity", "ID Photo URI: " + idPhotoUri.toString());

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference idPhotoRef = storageRef.child("id_photos/" + certification.getUserId() + ".jpg");

        idPhotoRef.putFile(idPhotoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("EnlistAsGuideActivity", "ID Photo uploaded successfully.");
                    idPhotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        certification.setIdPhotoUrl(uri.toString());
                        saveGuideProfileToFirebase(certification);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EnlistAsGuideActivity", "Failed to upload ID photo: " + e.getMessage());
                    Toast.makeText(EnlistAsGuideActivity.this, "Failed to upload ID photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveGuideProfileToFirebase(Certification certification) {
        FirebaseFirestore.getInstance().collection("certifications")
                .document(certification.getCertificationId()) // Use certificationId as the document ID
                .set(certification, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("EnlistAsGuideActivity", "Guide profile submitted for approval.");
                    Toast.makeText(EnlistAsGuideActivity.this, "Guide profile submitted for approval.", Toast.LENGTH_SHORT).show();

                    // Redirect to ProfileActivity
                    Intent intent = new Intent(EnlistAsGuideActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("EnlistAsGuideActivity", "Failed to submit guide profile.", e);
                    Toast.makeText(EnlistAsGuideActivity.this, "Failed to submit guide profile.", Toast.LENGTH_SHORT).show();
                });
    }

    private void selectCertificationFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }
}
