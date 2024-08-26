package com.example.myguidefirebase;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
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
        buttonEditSave.setOnClickListener(v -> toggleEditSave());
        buttonEditServices.setOnClickListener(v -> openEditServicesDialog());
        buttonEditLanguages.setOnClickListener(v -> openEditLanguagesDialog());
    }

    private void openEditLanguagesDialog() {
        // Define the available languages (you can also load this from resources)
        String[] languagesArray = {"English", "Hebrew", "Spanish", "French", "German", "Arabic",
                "Hindu", "Russian", "Hungarian", "Nigerian", "Ukrainian",
                "Japanese", "Korean", "Portuguese"};

        boolean[] checkedLanguages = new boolean[languagesArray.length]; // To keep track of checked items
        List<String> selectedLanguages = new ArrayList<>(Arrays.asList(textViewLanguages.getText().toString().split(", ")));

        for (int i = 0; i < languagesArray.length; i++) {
            checkedLanguages[i] = selectedLanguages.contains(languagesArray[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Languages");

        builder.setMultiChoiceItems(languagesArray, checkedLanguages, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedLanguages.add(languagesArray[which]);
            } else {
                selectedLanguages.remove(languagesArray[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Save selected languages and update the UI
            textViewLanguages.setText(TextUtils.join(", ", selectedLanguages));
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void openEditServicesDialog() {
        // Define the available services
        String[] servicesArray = {"Hiking Trips", "Museums", "Nightlife", "City Tours", "Food and Drink Tours",
                "Adventure Sports", "Historical Sites", "Art Galleries", "Shopping Tours", "Cultural Experiences"};

        boolean[] checkedServices = new boolean[servicesArray.length]; // To keep track of checked items
        List<String> selectedServices = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Services");

        builder.setMultiChoiceItems(servicesArray, checkedServices, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedServices.add(servicesArray[which]);
            } else {
                selectedServices.remove(servicesArray[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Save selected services and update the UI
            updateServicesUI(selectedServices);
        });

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

    private void loadUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Populate general user info
                    editTextName.setText(document.getString("name"));
                    editTextAbout.setText(document.getString("bio"));
                    editTextEmail.setText(document.getString("email"));
                    editTextPhone.setText(document.getString("phoneNumber"));

                    // Get user role
                    userRole = document.getString("role");

                    // Handle the 'languages' field
                    String languagesString = document.getString("languages");
                    if (languagesString != null) {
                        textViewLanguages.setText(languagesString);
                    }

                    // Handle the 'location' field as a Map
                    Object locationObject = document.get("location");
                    if (locationObject instanceof Map) {
                        Map<String, String> locationMap = (Map<String, String>) locationObject;
                        editTextLocation.setText(locationMap.get("country") + ", " + locationMap.get("city"));
                    }

                    // Check if the user is a guide and populate guide-specific fields
                    Boolean isGuide = document.getBoolean("guideInfo.isGuide");
                    if (isGuide != null && isGuide) {
                        guideFieldsLayout.setVisibility(View.VISIBLE); // Show guide fields
                        textViewCertificationStatus.setVisibility(View.VISIBLE); // Show certification status
                        textViewCertificationStatus.setText(document.getString("guideInfo.certificationStatus"));
                        editTextPricePerDay.setText(document.getString("guideInfo.pricePerDay"));

                        // Populate services checkboxes
                        Object servicesObject = document.get("guideInfo.services");
                        if (servicesObject instanceof List) {
                            List<String> servicesList = (List<String>) servicesObject;
                            populateServicesCheckboxes(servicesList);
                        }

                        if ("certified".equals(document.getString("guideInfo.certificationStatus"))) {
                            ImageView imageViewCertifiedLogo = findViewById(R.id.imageViewCertifiedLogo);
                            imageViewCertifiedLogo.setVisibility(View.VISIBLE);
                        }

                        if ("pending".equals(document.getString("guideInfo.certificationStatus"))) {
                            // Disable editing and show a message
                            enableEditing(false);
                            buttonEditSave.setEnabled(false);
                            Toast.makeText(ProfileActivity.this, "Your certification is pending approval.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditSave() {
        if (isEditMode) {
            // Save mode
            if (validateFields()) {
                saveUserProfile();
            }
        } else {
            // Edit mode
            enableEditing(true);
            buttonEditSave.setText("Save");
        }
        isEditMode = !isEditMode;
    }

    private void enableEditing(boolean enabled) {
        editTextName.setEnabled(enabled);
        editTextAbout.setEnabled(enabled);
        editTextEmail.setEnabled(false); // Email should not be editable
        editTextPhone.setEnabled(enabled);
        editTextLocation.setEnabled(enabled);
        editTextPricePerDay.setEnabled(enabled);

        // Enable or disable guide-specific fields if the user is a guide
        if ("guide".equals(userRole)) {
            buttonEditServices.setEnabled(enabled);
            buttonEditLanguages.setEnabled(enabled);
        }
    }

    private void saveUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        String name = editTextName.getText().toString();
        String about = editTextAbout.getText().toString();
        String phoneNumber = editTextPhone.getText().toString();
        String location = editTextLocation.getText().toString();
        String pricePerDay = editTextPricePerDay.getText().toString();

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("bio", about);
        userProfile.put("phoneNumber", phoneNumber);
        userProfile.put("location", parseLocation(location));

        // Save guide-specific info if the user is a guide
        if (guideFieldsLayout.getVisibility() == View.VISIBLE) {
            String languages = textViewLanguages.getText().toString();
            List<String> selectedServices = getSelectedServices();

            Map<String, Object> guideInfo = new HashMap<>();
            guideInfo.put("isGuide", true);
            guideInfo.put("languages", languages);
            guideInfo.put("services", selectedServices);
            guideInfo.put("pricePerDay", pricePerDay);
            guideInfo.put("certificationStatus", textViewCertificationStatus.getText().toString());

            userProfile.put("guideInfo", guideInfo);
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(userProfile, SetOptions.merge())
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

        if (guideFieldsLayout.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(editTextPricePerDay.getText().toString())) {
                editTextPricePerDay.setError("Price per day is required");
                return false;
            }
        }

        return true;
    }

    private Map<String, String> parseLocation(String location) {
        String[] locationParts = location.split(", ");
        String country = locationParts.length > 0 ? locationParts[0] : "";
        String city = locationParts.length > 1 ? locationParts[1] : "";

        Map<String, String> locationMap = new HashMap<>();
        locationMap.put("country", country);
        locationMap.put("city", city);

        return locationMap;
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
        for (String service : services) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(service);
            checkBox.setChecked(true);
            checkboxGroupServices.addView(checkBox);
        }
    }
}
