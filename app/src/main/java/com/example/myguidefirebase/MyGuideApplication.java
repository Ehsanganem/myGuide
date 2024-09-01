package com.example.myguidefirebase;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MyGuideApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}
