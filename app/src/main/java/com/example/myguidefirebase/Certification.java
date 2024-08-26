package com.example.myguidefirebase;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class Certification {

    private String certificationId;
    private String userId;
    private String userName;
    private String userEmail;
    private String certificationUrl;
    private String idPhotoUrl;
    private String userRequest;
    private String status; // e.g., "pending", "approved", "rejected"
    private String adminComments;
    private boolean isCertified;
    private Map<String, String> location;  // New location field

    public Certification() {
        // Default constructor
    }

    public Certification(String userId, String userName, String userEmail, boolean isCertified) {
        this.certificationId = FirebaseFirestore.getInstance().collection("certifications").document().getId(); // Generate and set the ID
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.status = "pending";
        this.isCertified = isCertified;
    }

    // Getter and Setter methods
    public String getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(String certificationId) {
        this.certificationId = certificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCertificationUrl() {
        return certificationUrl;
    }

    public void setCertificationUrl(String certificationUrl) {
        this.certificationUrl = certificationUrl;
    }

    public String getIdPhotoUrl() {
        return idPhotoUrl;
    }

    public void setIdPhotoUrl(String idPhotoUrl) {
        this.idPhotoUrl = idPhotoUrl;
    }

    public String getUserRequest() {
        return userRequest;
    }

    public void setUserRequest(String userRequest) {
        this.userRequest = userRequest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminComments() {
        return adminComments;
    }

    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
    }

    public boolean isCertified() {
        return isCertified;
    }

    public void setCertified(boolean certified) {
        isCertified = certified;
    }

    // New getter and setter for location
    public Map<String, String> getLocation() {
        return location;
    }

    public void setLocation(Map<String, String> location) {
        this.location = location;
    }
}
