package com.example.myguidefirebase;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchGuidesActivity extends AppCompatActivity {

    private EditText editTextLocation;
    private Spinner spinnerLanguages, spinnerServices;
    private Button buttonSearch;
    private RecyclerView recyclerViewGuides;
    private GuideAdapter guideAdapter;
    private List<Guide> guideList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_guides);

        editTextLocation = findViewById(R.id.editTextLocation);
        spinnerLanguages = findViewById(R.id.spinnerLanguages);
        spinnerServices = findViewById(R.id.spinnerServices);
        buttonSearch = findViewById(R.id.buttonSearch);
        recyclerViewGuides = findViewById(R.id.recyclerViewGuides);

        recyclerViewGuides.setLayoutManager(new LinearLayoutManager(this));
        guideAdapter = new GuideAdapter(this, guideList, guide -> {
            // Handle guide click (e.g., open GuideDetailActivity)
        });
        recyclerViewGuides.setAdapter(guideAdapter);

        buttonSearch.setOnClickListener(v -> searchGuides());
    }

    private void searchGuides() {
        String location = editTextLocation.getText().toString().trim();
        String language = spinnerLanguages.getSelectedItem().toString();
        String service = spinnerServices.getSelectedItem().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("guides");

        // Apply filters based on user input
        if (!location.isEmpty()) {
            query = query.whereEqualTo("location.country", location);
        }
        if (!language.equals("Any")) {
            query = query.whereArrayContains("languages", language);
        }
        if (!service.equals("Any")) {
            query = query.whereArrayContains("services", service);
        }

        // Execute the query and update the RecyclerView
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                guideList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Guide guide = document.toObject(Guide.class);
                    guideList.add(guide);
                }
                guideAdapter.notifyDataSetChanged();
            } else {
                // Handle query failure
            }
        });
    }
}
