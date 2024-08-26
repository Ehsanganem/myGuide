package com.example.myguidefirebase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuideManager {

    private FirebaseFirestore firestore;

    public GuideManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void saveGuideProfile(User user, OnGuideProfileSaveListener listener) {
        String userId = user.getUserId();

        Map<String, Object> guideProfile = new HashMap<>();
        guideProfile.put("userId", userId);
        guideProfile.put("name", user.getName());
        guideProfile.put("bio", user.getBio());
        guideProfile.put("email", user.getEmail());
        guideProfile.put("phoneNumber", user.getPhoneNumber());
        guideProfile.put("location", user.getLocation()); // Map<String, String>
        guideProfile.put("role", user.getRole()); // "certified_guide", "uncertified_guide"
        guideProfile.put("certificationStatus", user.getCertificationStatus());
        guideProfile.put("certificationUrl", user.getCertificationUrl());
        guideProfile.put("idPhotoUrl", user.getIdPhotoUrl());
        guideProfile.put("pricePerDay", user.getPricePerDay());
        guideProfile.put("services", user.getServices());
        guideProfile.put("languages", user.getLanguages());

        firestore.collection("users")
                .document(userId)
                .set(guideProfile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateGuideCertificationStatus(String userId, String status, OnGuideProfileSaveListener listener) {
        firestore.collection("users")
                .document(userId)
                .update("certificationStatus", status)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnGuideProfileSaveListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
