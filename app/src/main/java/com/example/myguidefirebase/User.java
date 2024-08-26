package com.example.myguidefirebase;

import java.util.List;
import java.util.Map;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String bio;
    private String certificationStatus;
    private String certificationUrl;
    private String idPhotoUrl;
    private boolean isProfileComplete;
    private String role;  // "user", "certified_guide", "uncertified_guide"
    private List<String> languages;
    private Map<String, String> location; // Map with fields like country and city
    private String pricePerDay;
    private List<String> services;

    // Default constructor (required for Firebase)
    public User() {}

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCertificationStatus() { return certificationStatus; }
    public void setCertificationStatus(String certificationStatus) { this.certificationStatus = certificationStatus; }

    public String getCertificationUrl() { return certificationUrl; }
    public void setCertificationUrl(String certificationUrl) { this.certificationUrl = certificationUrl; }

    public String getIdPhotoUrl() { return idPhotoUrl; }
    public void setIdPhotoUrl(String idPhotoUrl) { this.idPhotoUrl = idPhotoUrl; }

    public boolean isProfileComplete() { return isProfileComplete; }
    public void setProfileComplete(boolean profileComplete) { isProfileComplete = profileComplete; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public Map<String, String> getLocation() { return location; }
    public void setLocation(Map<String, String> location) { this.location = location; }

    public String getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(String pricePerDay) { this.pricePerDay = pricePerDay; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

    // Role-related methods
    public boolean isGuide() {
        return "certified_guide".equals(role) || "uncertified_guide".equals(role);
    }

    public boolean isCertifiedGuide() {
        return "certified_guide".equals(role);
    }

    public boolean isUncertifiedGuide() {
        return "uncertified_guide".equals(role);
    }
}
