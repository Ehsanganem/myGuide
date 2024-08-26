package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileSetupActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextLocation;
    private MultiAutoCompleteTextView editTextLanguages;
    private Button buttonSave;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_setup);

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextLanguages = findViewById(R.id.editTextLanguages);
        buttonSave = findViewById(R.id.buttonSave);

        auth = FirebaseAuth.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up MultiAutoCompleteTextView for languages
        String[] languages = {"English", "Hebrew", "Spanish", "French", "German", "Chinese"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, languages);
        editTextLanguages.setAdapter(adapter);
        editTextLanguages.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Set up save button listener
        buttonSave.setOnClickListener(v -> {
            if (validateFields()) {
                saveUserProfile();
            }
        });
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

        if (TextUtils.isEmpty(editTextLanguages.getText().toString())) {
            editTextLanguages.setError("At least one language is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextLocation.getText().toString())) {
            editTextLocation.setError("Location is required");
            return false;
        }

        return true;
    }

    private void saveUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        String name = editTextName.getText().toString();
        String phoneNumber = editTextPhone.getText().toString();
        List<String> languages = Arrays.asList(editTextLanguages.getText().toString().split(",\\s*"));
        String locationString = editTextLocation.getText().toString();

        // Assuming location is stored as a map with country and city.
        Map<String, String> location = new HashMap<>();
        location.put("country", locationString);  // You may split this into country/city if required.

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("phoneNumber", phoneNumber);
        userProfile.put("languages", languages);  // Store as a List<String>
        userProfile.put("location", location);  // Store as a Map<String, String>
        userProfile.put("role", "user");  // Default role as user
        userProfile.put("isProfileComplete", true);  // Profile is now complete

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(userProfile, SetOptions.merge())  // Use merge to avoid overwriting other fields
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
