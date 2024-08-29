package com.example.myguidefirebase;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mukesh.countrypicker.CountryPicker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextName, editTextAbout, editTextEmail, editTextPhone, editTextPricePerDay;
    private TextView textViewLocation, textViewLanguages;
    private Button buttonSelectCountry, buttonEditServices, buttonEditSave, buttonEditLanguages;
    private ImageView imageViewProfile, imageViewEditProfilePicture, imageViewCertifiedLogo;
    private LinearLayout checkboxGroupServices;
    private CardView guideFieldsLayout;
    private boolean isEditMode = false;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;
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
        textViewLocation = findViewById(R.id.textViewSelectedCountry);
        editTextPricePerDay = findViewById(R.id.editTextPricePerDay);
        checkboxGroupServices = findViewById(R.id.checkboxGroupServices);
        guideFieldsLayout = findViewById(R.id.guideFieldsLayout);
        textViewLanguages = findViewById(R.id.textViewLanguages);
        buttonSelectCountry = findViewById(R.id.buttonSelectCountry);
        buttonEditServices = findViewById(R.id.buttonEditServices);
        buttonEditSave = findViewById(R.id.buttonEditSave);
        buttonEditLanguages = findViewById(R.id.buttonEditLanguages);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        imageViewEditProfilePicture = findViewById(R.id.imageViewEditProfilePicture);
        imageViewCertifiedLogo = findViewById(R.id.imageViewCertifiedLogo);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userId = auth.getCurrentUser().getUid();

        // Load user profile data
        loadUserProfile();

        // Set up button click listeners
        buttonEditSave.setOnClickListener(v -> toggleEditSave());
        buttonEditServices.setOnClickListener(v -> openEditServicesDialog());
        buttonEditLanguages.setOnClickListener(v -> openEditLanguagesDialog());
        buttonSelectCountry.setOnClickListener(v -> showCountryPicker());
        imageViewEditProfilePicture.setOnClickListener(v -> showProfilePictureOptions());
    }

    private void loadUserProfile() {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
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
        editTextName.setText(user.getName());
        editTextAbout.setText(user.getBio());
        editTextEmail.setText(user.getEmail());
        editTextPhone.setText(user.getPhoneNumber());
        textViewLocation.setText(user.getLocation().get("country"));

        if (user.getRole() != null) {
            userRole = user.getRole();
            if (user.isGuide()) {
                guideFieldsLayout.setVisibility(View.VISIBLE);
                editTextPricePerDay.setText(user.getPricePerDay());
                populateServicesCheckboxes(user.getServices());
                textViewLanguages.setText(TextUtils.join(", ", user.getLanguages()));
                if (user.isCertifiedGuide()) {
                    imageViewCertifiedLogo.setVisibility(View.VISIBLE);
                }
            }
        }

        Glide.with(this).load(user.getIdPhotoUrl()).into(imageViewProfile);
    }

    private void toggleEditSave() {
        if (isEditMode) {
            if (validateFields()) {
                saveUserProfile();
            }
        } else {
            enableEditing(true);
            buttonEditSave.setText("Save");
        }
        isEditMode = !isEditMode;
    }

    private void enableEditing(boolean enabled) {
        editTextName.setEnabled(enabled);
        editTextAbout.setEnabled(enabled);
        editTextPhone.setEnabled(enabled);
        buttonSelectCountry.setEnabled(enabled);
        if (userRole != null && (userRole.equals("certified_guide") || userRole.equals("uncertified_guide"))) {
            buttonEditServices.setEnabled(enabled);
            buttonEditLanguages.setEnabled(enabled);
        }
    }

    private void saveUserProfile() {
        Map<String, Object> userProfileUpdates = new HashMap<>();
        userProfileUpdates.put("name", editTextName.getText().toString());
        userProfileUpdates.put("bio", editTextAbout.getText().toString());
        userProfileUpdates.put("phoneNumber", editTextPhone.getText().toString());

        // Save location as a Map
        Map<String, String> locationMap = new HashMap<>();
        locationMap.put("country", textViewLocation.getText().toString());
        userProfileUpdates.put("location", locationMap);
        userProfileUpdates.put("role", userRole);

        if (guideFieldsLayout.getVisibility() == View.VISIBLE) {
            userProfileUpdates.put("pricePerDay", editTextPricePerDay.getText().toString());
            userProfileUpdates.put("languages", getSelectedLanguages());
            userProfileUpdates.put("services", getSelectedServices());
        }

        db.collection("users").document(userId)
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

    private void showCountryPicker() {
        CountryPicker picker = new CountryPicker.Builder().with(this)
                .listener(country -> textViewLocation.setText(country.getName()))
                .build();

        picker.showDialog(ProfileActivity.this);
    }

    private void showProfilePictureOptions() {
        PopupMenu popupMenu = new PopupMenu(this, imageViewEditProfilePicture);
        popupMenu.getMenuInflater().inflate(R.menu.profile_image_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(this::onProfilePictureOptionSelected);
        popupMenu.show();
    }

    private boolean onProfilePictureOptionSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_upload || itemId == R.id.action_change) {
            openFileChooser();
            return true;
        } else if (itemId == R.id.action_delete) {
            deleteProfileImage();
            return true;
        } else {
            return false;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                uploadProfileImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadProfileImage(Bitmap bitmap) {
        StorageReference storageRef = storage.getReference("profile_images/" + userId + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            updateProfileImageInFirestore(imageUrl);
        })).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProfileImageInFirestore(String imageUrl) {
        db.collection("users").document(userId)
                .update("idPhotoUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    loadImage(imageUrl);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteProfileImage() {
        StorageReference storageRef = storage.getReference("profile_images/" + userId + ".jpg");
        storageRef.delete().addOnSuccessListener(aVoid -> {
            String defaultImageUrl = "url_of_your_default_empty_image";  // Replace with your default image URL
            updateProfileImageInFirestore(defaultImageUrl);
            loadImage(defaultImageUrl);
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "Failed to delete profile picture", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadImage(String imageUrl) {
        Glide.with(this).load(imageUrl).into(imageViewProfile);
    }
}
