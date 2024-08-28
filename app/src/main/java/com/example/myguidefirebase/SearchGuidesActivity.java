package com.example.myguidefirebase;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mukesh.countrypicker.CountryPicker;

import java.util.ArrayList;
import java.util.List;

public class SearchGuidesActivity extends AppCompatActivity {

    private Button buttonSelectCountry, buttonSelectLanguages, buttonSelectServices, buttonSearch;
    private TextView textViewSelectedCountry, textViewSelectedLanguages, textViewSelectedServices;
    private RecyclerView recyclerViewSearchResults;

    private List<String> selectedLanguages = new ArrayList<>();
    private List<String> selectedServices = new ArrayList<>();
    private String selectedCountry;

    private FirebaseFirestore db;
    private UsersAdapter usersAdapter;
    private List<User> guidesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_guides);

        db = FirebaseFirestore.getInstance();

        buttonSelectCountry = findViewById(R.id.buttonSelectCountry);
        textViewSelectedCountry = findViewById(R.id.textViewSelectedCountry);
        buttonSelectLanguages = findViewById(R.id.buttonSelectLanguages);
        buttonSelectServices = findViewById(R.id.buttonSelectServices);
        buttonSearch = findViewById(R.id.buttonSearch);
        textViewSelectedLanguages = findViewById(R.id.textViewSelectedLanguages);
        textViewSelectedServices = findViewById(R.id.textViewSelectedServices);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);

        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new UsersAdapter(guidesList);
        recyclerViewSearchResults.setAdapter(usersAdapter);

        // Handle country selection
        buttonSelectCountry.setOnClickListener(v -> showCountryPicker());

        // Handle language selection
        buttonSelectLanguages.setOnClickListener(v -> openLanguageSelectionDialog());

        // Handle services selection
        buttonSelectServices.setOnClickListener(v -> openServicesSelectionDialog());

        // Handle search button click
        buttonSearch.setOnClickListener(v -> performSearch());
    }

    private void showCountryPicker() {
        CountryPicker picker = new CountryPicker.Builder().with(this)
                .listener(country -> {
                    selectedCountry = country.getName();
                    textViewSelectedCountry.setText(selectedCountry);
                }).build();

        picker.showDialog(SearchGuidesActivity.this);
    }

    private void openLanguageSelectionDialog() {
        // Multi-selection dialog for languages
        String[] languagesArray = {"English", "Hebrew", "Spanish", "French", "German", "Arabic", "Russian"};

        boolean[] checkedLanguages = new boolean[languagesArray.length];
        for (int i = 0; i < languagesArray.length; i++) {
            checkedLanguages[i] = selectedLanguages.contains(languagesArray[i]);
        }

        new AlertDialog.Builder(this)
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

    private void openServicesSelectionDialog() {
        // Multi-selection dialog for services
        String[] servicesArray = {"Hiking Trips", "Museums", "Nightlife", "City Tours", "Food and Drink Tours"};

        boolean[] checkedServices = new boolean[servicesArray.length];
        for (int i = 0; i < servicesArray.length; i++) {
            checkedServices[i] = selectedServices.contains(servicesArray[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Services")
                .setMultiChoiceItems(servicesArray, checkedServices, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedServices.add(servicesArray[which]);
                    } else {
                        selectedServices.remove(servicesArray[which]);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> textViewSelectedServices.setText(selectedServices.toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSearch() {
        // Use the selectedCountry variable instead of spinnerCountry
        if (selectedCountry == null || selectedCountry.isEmpty()) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLanguages.isEmpty()) {
            Toast.makeText(this, "Please select at least one language", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedServices.isEmpty()) {
            Toast.makeText(this, "Please select at least one service", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform the first query with the country filter
        Query query = db.collection("users")
                .whereEqualTo("role", "certified_guide") // Filter for certified guides
                .whereEqualTo("location.country", selectedCountry);

        // Add filters for the languages and services using whereArrayContains
        if (!selectedLanguages.isEmpty()) {
            query = query.whereArrayContains("languages", selectedLanguages.get(0)); // Example for one language
        }

        // Execute the query
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> initialResult = task.getResult().toObjects(User.class);

                        // Filter results by services manually
                        List<User> filteredResult = new ArrayList<>();
                        for (User user : initialResult) {
                            if (user.getServices().containsAll(selectedServices)) {
                                filteredResult.add(user);
                            }
                        }

                        // Update the RecyclerView with the filtered results
                        guidesList.clear();
                        guidesList.addAll(filteredResult);
                        usersAdapter.notifyDataSetChanged();

                        if (filteredResult.isEmpty()) {
                            Toast.makeText(SearchGuidesActivity.this, "No guides found matching your criteria", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("SearchGuidesActivity", "Error getting guides", task.getException());
                        Toast.makeText(SearchGuidesActivity.this, "Error retrieving guides", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
