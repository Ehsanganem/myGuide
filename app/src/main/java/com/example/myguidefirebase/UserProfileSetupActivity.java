package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mukesh.countrypicker.CountryPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileSetupActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone;
    private TextView textViewSelectedCountry, textViewSelectedLanguages;
    private Button buttonSelectCountry, buttonSelectLanguages, buttonSave;
    private FirebaseAuth auth;
    private String selectedCountry;
    private List<String> selectedLanguages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_setup);

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        textViewSelectedCountry = findViewById(R.id.textViewSelectedCountry);
        textViewSelectedLanguages = findViewById(R.id.textViewSelectedLanguages);
        buttonSelectCountry = findViewById(R.id.buttonSelectCountry);
        buttonSelectLanguages = findViewById(R.id.buttonSelectLanguages);
        buttonSave = findViewById(R.id.buttonSave);

        auth = FirebaseAuth.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up country picker
        buttonSelectCountry.setOnClickListener(v -> showCountryPicker());

        // Set up languages selection dialog
        buttonSelectLanguages.setOnClickListener(v -> openLanguageSelectionDialog());

        // Set up save button listener
        buttonSave.setOnClickListener(v -> {
            if (validateFields()) {
                saveUserProfile();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button click here
            finish(); // Close the activity and go back to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showCountryPicker() {
        CountryPicker picker = new CountryPicker.Builder().with(this)
                .listener(country -> {
                    selectedCountry = country.getName();
                    textViewSelectedCountry.setText(selectedCountry);
                }).build();

        picker.showDialog(UserProfileSetupActivity.this);
    }

    private void openLanguageSelectionDialog() {
        // Multi-selection dialog for languages
        String[] languagesArray = {"English", "Hebrew", "Spanish", "French", "German", "Chinese", "Arabic", "Russian"};

        boolean[] checkedLanguages = new boolean[languagesArray.length];
        for (int i = 0; i < languagesArray.length; i++) {
            checkedLanguages[i] = selectedLanguages.contains(languagesArray[i]);
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Languages")
                .setMultiChoiceItems(languagesArray, checkedLanguages, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedLanguages.add(languagesArray[which]);
                    } else {
                        selectedLanguages.remove(languagesArray[which]);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> textViewSelectedLanguages.setText(selectedLanguages.toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(editTextName.getText().toString())) {
            editTextName.setError("Name is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextPhone.getText().toString())) {
            editTextPhone.setError("Phone number is required");
            return false;
        }

        if (TextUtils.isEmpty(selectedCountry)) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedLanguages.isEmpty()) {
            Toast.makeText(this, "Please select at least one language", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        String name = editTextName.getText().toString();
        String phoneNumber = editTextPhone.getText().toString();

        // Assuming location is stored as a map with country.
        Map<String, String> location = new HashMap<>();
        location.put("country", selectedCountry);

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("phoneNumber", phoneNumber);
        userProfile.put("languages", selectedLanguages);
        userProfile.put("location", location);
        userProfile.put("role", "user");  // Default role as user
        userProfile.put("isProfileComplete", true);  // Profile is now complete

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(userProfile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserProfileSetupActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to MainActivity after saving
                    Intent intent = new Intent(UserProfileSetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close the activity after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfileSetupActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                });
    }
}
