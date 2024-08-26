package com.example.myguidefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class PendingCertificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PendingCertificationsAdapter adapter;
    private List<Certification> pendingCertifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_certifications);

        recyclerView = findViewById(R.id.recyclerViewPendingCertifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingCertifications = new ArrayList<>();
        adapter = new PendingCertificationsAdapter(pendingCertifications, certification -> {
            Intent intent = new Intent(PendingCertificationsActivity.this, CertificationDetailActivity.class);
            intent.putExtra("certificationId", certification.getCertificationId());
            intent.putExtra("userId", certification.getUserId());  // Passing userId
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        fetchPendingCertifications();
    }

    private void fetchPendingCertifications() {
        FirebaseFirestore.getInstance().collection("certifications")
                .whereEqualTo("status", "pending") // Ensure this matches the field name in Firestore
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pendingCertifications.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Certification certification = document.toObject(Certification.class);
                            Log.d("PendingCertifications", "Fetched: " + certification.getUserName());
                            pendingCertifications.add(certification);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(PendingCertificationsActivity.this, "Failed to load certifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
