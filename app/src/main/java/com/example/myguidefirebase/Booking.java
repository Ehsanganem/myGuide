package com.example.myguidefirebase;

import java.io.Serializable;
import java.util.Date;

public class Booking implements Serializable {
    private String bookingId;
    private String touristId;
    private String guideId;
    private String guideName;
    private Date startDate;
    private Date endDate;
    private double totalCost;
    private String status; // "pending", "confirmed", "completed", "canceled"
    private String confirmationNumber; // New field for confirmation number

    public Booking() {
        // Default constructor required for calls to DataSnapshot.getValue(Booking.class)
    }

    public Booking(String bookingId, String touristId, String guideId, String guideName, Date startDate, Date endDate, double totalCost, String status, String confirmationNumber) {
        this.bookingId = bookingId;
        this.touristId = touristId;
        this.guideId = guideId;
        this.guideName = guideName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.status = status;
        this.confirmationNumber = confirmationNumber;
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTouristId() {
        return touristId;
    }

    public void setTouristId(String touristId) {
        this.touristId = touristId;
    }

    public String getGuideId() {
        return guideId;
    }

    public void setGuideId(String guideId) {
        this.guideId = guideId;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }
}
