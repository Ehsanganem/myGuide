package com.example.myguidefirebase;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ProfileActivity extends AppCompatActivity {

    private EditText editTextName, editTextAbout, editTextEmail, editTextPhone, editTextLocation, editTextPricePerDay;
    private LinearLayout checkboxGroupServices;

    private CardView guideFieldsLayout;
    private TextView textViewCertificationStatus, textViewLanguages;
    private Button buttonEditServices, buttonEditSave, buttonEditLanguages;
    private boolean isEditMode = false;
    private FirebaseAuth auth;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI elements
        editTextName = findViewById(R.id.editTextName);
        editTextAbout = findViewById(R.id.editTextAbout);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextPricePerDay = findViewById(R.id.editTextPricePerDay);
        checkboxGroupServices = findViewById(R.id.checkboxGroupServices);
        guideFieldsLayout = findViewById(R.id.guideFieldsLayout);
        textViewCertificationStatus = findViewById(R.id.textViewCertificationStatus);
        textViewLanguages = findViewById(R.id.textViewLanguages);
        buttonEditServices = findViewById(R.id.buttonEditServices);
        buttonEditSave = findViewById(R.id.buttonEditSave);
        buttonEditLanguages = findViewById(R.id.buttonEditLanguages);
        auth = FirebaseAuth.getInstance();

        // Load user profile data
        loadUserProfile();

        // Set up button click listeners
        buttonEditSave.setOnClickListener(v -> {
            Log.d("ProfileActivity", "Edit/Save button clicked");
            toggleEditSave();
        });

        buttonEditServices.setOnClickListener(v -> openEditServicesDialog());
        buttonEditLanguages.setOnClickListener(v -> openEditLanguagesDialog());
    }

    private void loadUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        populateUIWithUserData(user);
                    }
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUIWithUserData(User user) {
        // Populate general user info
        editTextName.setText(user.getName());
        editTextAbout.setText(user.getBio());
        editTextEmail.setText(user.getEmail());
        editTextPhone.setText(user.getPhoneNumber());

        // Set role and handle guide-specific UI elements
        userRole = user.getRole();
        if (user.isGuide()) {
            guideFieldsLayout.setVisibility(View.VISIBLE);
            textViewCertificationStatus.setVisibility(View.VISIBLE);
            textViewCertificationStatus.setText(user.getCertificationStatus());
            editTextPricePerDay.setText(user.getPricePerDay());

            populateServicesCheckboxes(user.getServices());

            // Display the list of languages as a comma-separated string
            textViewLanguages.setText(TextUtils.join(", ", user.getLanguages()));

            // Handle location as a Map<String, String>
            if (user.getLocation() != null) {
                Map<String, String> location = user.getLocation();
                editTextLocation.setText(location.get("country")); // Adjust as needed
            }

            if (user.isCertifiedGuide()) {
                ImageView imageViewCertifiedLogo = findViewById(R.id.imageViewCertifiedLogo);
                imageViewCertifiedLogo.setVisibility(View.VISIBLE);
            }

            // Allow editing even if certification is pending
            if ("pending".equals(user.getCertificationStatus())) {
                Toast.makeText(ProfileActivity.this, "Your certification is pending approval.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleEditSave() {
        Log.d("ProfileActivity", "toggleEditSave called. isEditMode: " + isEditMode);
        if (isEditMode) {
            if (validateFields()) {
                saveUserProfile();
                Log.d("ProfileActivity", "Profile saved.");
            } else {
                Log.d("ProfileActivity", "Validation failed.");
            }
        } else {
            enableEditing(true);
            buttonEditSave.setText("Save");
            Log.d("ProfileActivity", "Editing enabled.");
        }
        isEditMode = !isEditMode;
    }

    private void enableEditing(boolean enabled) {
        editTextName.setEnabled(enabled);
        editTextAbout.setEnabled(enabled);
        editTextEmail.setEnabled(false);  // Email should not be editable
        editTextPhone.setEnabled(enabled);
        editTextLocation.setEnabled(enabled);
        editTextPricePerDay.setEnabled(enabled);

        if (userRole != null && (userRole.equals("certified_guide") || userRole.equals("uncertified_guide"))) {
            buttonEditServices.setEnabled(enabled);
            buttonEditLanguages.setEnabled(enabled);
        }
    }

    private void saveUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> userProfileUpdates = new HashMap<>();
        userProfileUpdates.put("name", editTextName.getText().toString());
        userProfileUpdates.put("bio", editTextAbout.getText().toString());
        userProfileUpdates.put("phoneNumber", editTextPhone.getText().toString());

        // Save location as a Map
        String locationString = editTextLocation.getText().toString();
        Map<String, String> locationMap = new HashMap<>();
        locationMap.put("country", locationString);
        userProfileUpdates.put("location", locationMap);

        userProfileUpdates.put("role", userRole);

        if (guideFieldsLayout.getVisibility() == View.VISIBLE) {
            userProfileUpdates.put("pricePerDay", editTextPricePerDay.getText().toString());
            userProfileUpdates.put("languages", getSelectedLanguages());  // Ensure this is a list
            userProfileUpdates.put("services", getSelectedServices());
            userProfileUpdates.put("certificationStatus", textViewCertificationStatus.getText().toString());
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(userProfileUpdates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                    enableEditing(false);
                    buttonEditSave.setText("Edit");
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show());
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

        if (guideFieldsLayout.getVisibility() == View.VISIBLE && TextUtils.isEmpty(editTextPricePerDay.getText().toString())) {
            editTextPricePerDay.setError("Price per day is required");
            return false;
        }

        return true;
    }

    private List<String> getSelectedLanguages() {
        // Split the languages string into a list
        return Arrays.asList(textViewLanguages.getText().toString().split(",\\s*"));
    }

    private List<String> getSelectedServices() {
        List<String> selectedServices = new ArrayList<>();
        int childCount = checkboxGroupServices.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = checkboxGroupServices.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    selectedServices.add(checkBox.getText().toString());
                }
            }
        }
        return selectedServices;
    }

    private void populateServicesCheckboxes(List<String> services) {
        checkboxGroupServices.removeAllViews();
        if (services != null) {
            for (String service : services) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(service);
                checkBox.setChecked(true);
                checkboxGroupServices.addView(checkBox);
            }
        }
    }

    private void openEditServicesDialog() {
        String[] servicesArray = {"Hiking Trips", "Museums", "Nightlife", "City Tours", "Food and Drink Tours",
                "Adventure Sports", "Historical Sites", "Art Galleries", "Shopping Tours", "Cultural Experiences"};

        boolean[] checkedServices = new boolean[servicesArray.length];
        List<String> selectedServices = getSelectedServices();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Services");

        builder.setMultiChoiceItems(servicesArray, checkedServices, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedServices.add(servicesArray[which]);
            } else {
                selectedServices.remove(servicesArray[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> updateServicesUI(selectedServices));
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void updateServicesUI(List<String> selectedServices) {
        checkboxGroupServices.removeAllViews();
        for (String service : selectedServices) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(service);
            checkBox.setChecked(true);
            checkboxGroupServices.addView(checkBox);
        }
    }

    private void openEditLanguagesDialog() {
        String[] languagesArray = {"English", "Hebrew", "Spanish", "French", "German", "Arabic",
                "Hindi", "Russian", "Hungarian", "Nigerian", "Ukrainian",
                "Japanese", "Korean", "Portuguese"};

        boolean[] checkedLanguages = new boolean[languagesArray.length];
        List<String> selectedLanguages = new ArrayList<>(Arrays.asList(textViewLanguages.getText().toString().split(",\\s*")));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Languages");

        builder.setMultiChoiceItems(languagesArray, checkedLanguages, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedLanguages.add(languagesArray[which]);
            } else {
                selectedLanguages.remove(languagesArray[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> textViewLanguages.setText(TextUtils.join(", ", selectedLanguages)));
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }
}

