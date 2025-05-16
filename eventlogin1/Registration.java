package com.example.eventlogin1;

public class Registration {
    private String selectedDate;
    private String fullname;
    private String sapId;
    private String email;
    private String mobile;
    private String year;
    private String eventId;
    private String eventTitle;  // New field

    // Constructor
    public Registration(String selectedDate, String fullname, String sapId, String email, String mobile, String year, String eventId, String eventTitle) {
        this.selectedDate = selectedDate;
        this.fullname = fullname;
        this.sapId = sapId;
        this.email = email;
        this.mobile = mobile;
        this.year = year;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
    }

    // Default constructor for Firebase
    public Registration() {}

    // Getters and Setters
    public String getSelectedDate() { return selectedDate; }
    public void setSelectedDate(String selectedDate) { this.selectedDate = selectedDate; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getSapId() { return sapId; }
    public void setSapId(String sapId) { this.sapId = sapId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
}
