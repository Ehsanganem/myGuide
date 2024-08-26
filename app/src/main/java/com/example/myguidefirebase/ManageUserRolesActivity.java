package com.example.myguidefirebase;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class ManageUserRolesActivity extends AppCompatActivity {

    private static final String TAG = "ManageUserRolesActivity";

    private RecyclerView recyclerView;
    private UserRolesAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user_roles);

        initializeViews();
        initializeFirebase();
        fetchUsers();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewUserRoles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserRolesAdapter(userList, this);
        recyclerView.setAdapter(adapter);
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user found.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no user is authenticated
        }
    }

    private void fetchUsers() {
        userListener = db.collection("users")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error fetching users: ", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        userList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Log.d(TAG, "Fetched document: " + document.getData());
                            User user = document.toObject(User.class);
                            if (user != null) {
                                Log.d(TAG, "Parsed user: " + user.getName() + ", ID: " + user.getUserId());
                            }
                            if (user != null && user.getUserId() != null && !user.getUserId().equals(currentUser.getUid())) {
                                userList.add(user);
                            } else {
                                Log.d(TAG, "Skipped current user or invalid user document.");
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (userList.isEmpty()) {
                            Log.d(TAG, "User list is empty.");
                        }
                    } else {
                        Log.d(TAG, "QueryDocumentSnapshots is null.");
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
        }
    }
}
