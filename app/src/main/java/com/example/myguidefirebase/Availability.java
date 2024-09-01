package com.example.myguidefirebase;

import java.util.Date;

public class Availability {
    private String guideId;
    private Date startDate;
    private Date endDate;

    public Availability() {
        // Default constructor required for Firebase
    }

    public Availability(String guideId, Date startDate, Date endDate) {
        this.guideId = guideId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public String getGuideId() {
        return guideId;
    }

    public void setGuideId(String guideId) {
        this.guideId = guideId;
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
}
