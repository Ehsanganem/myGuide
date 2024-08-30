//package com.example.myguidefirebase;
//
//import java.util.List;
//import java.util.Map;
//
//public class Guide {
//
//    private String userId;
//    private String name;
//    private String email;
//    private String phoneNumber;
//    private String bio;
//    private String certificationStatus;
//    private String certificationUrl;
//    private String idPhotoUrl;  // Add this field
//    private boolean isCertified;  // Add this field
//    private Map<String, Object> location; // Location is a Map with fields like country and city
//    private String pricePerDay;
//    private List<String> services;
//    private List<String> languages;
//
//    // Default constructor (required for Firebase)
//    public Guide() {}
//
//    // Getters and Setters
//    public String getUserId() {
//        return userId;
//    }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPhoneNumber() {
//        return phoneNumber;
//    }
//
//    public void setPhoneNumber(String phoneNumber) {
//        this.phoneNumber = phoneNumber;
//    }
//
//    public String getBio() {
//        return bio;
//    }
//
//    public void setBio(String bio) {
//        this.bio = bio;
//    }
//
//    public String getCertificationStatus() {
//        return certificationStatus;
//    }
//
//    public void setCertificationStatus(String certificationStatus) {
//        this.certificationStatus = certificationStatus;
//    }
//
//    public String getCertificationUrl() {
//        return certificationUrl;
//    }
//
//    public void setCertificationUrl(String certificationUrl) {
//        this.certificationUrl = certificationUrl;
//    }
//
//    public String getIdPhotoUrl() {  // Add this getter
//        return idPhotoUrl;
//    }
//
//    public void setIdPhotoUrl(String idPhotoUrl) {  // Add this setter
//        this.idPhotoUrl = idPhotoUrl;
//    }
//
//    public boolean isCertified() {  // Add this getter
//        return isCertified;
//    }
//
//    public void setCertified(boolean isCertified) {  // Add this setter
//        this.isCertified = isCertified;
//    }
//
//    public Map<String, Object> getLocation() {
//        return location;
//    }
//
//    public void setLocation(Map<String, Object> location) {
//        this.location = location;
//    }
//
//    public String getPricePerDay() {
//        return pricePerDay;
//    }
//
//    public void setPricePerDay(String pricePerDay) {
//        this.pricePerDay = pricePerDay;
//    }
//
//    public List<String> getServices() {
//        return services;
//    }
//
//    public void setServices(List<String> services) {
//        this.services = services;
//    }
//
//    public List<String> getLanguages() {
//        return languages;
//    }
//
//    public void setLanguages(List<String> languages) {
//        this.languages = languages;
//    }
//}
