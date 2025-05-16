package com.example.eventlogin1;
public class Event {
    private String eventId;
    private String title;
    private String description;
    private String date;
    private String time;
    private String venue;
    private String guestSpeaker;
    private String registerLink;
    private String localImagePath;  // For local image

    // No-argument constructor
    public Event() {
    }

    public Event(String title, String description, String date, String time, String venue, String guestSpeaker, String registerLink, String localImagePath) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.venue = venue;
        this.guestSpeaker = guestSpeaker;
        this.registerLink = registerLink;
        this.localImagePath = localImagePath;
    }


    // Getters and setters for all fields
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getGuestSpeaker() {
        return guestSpeaker;
    }

    public void setGuestSpeaker(String guestSpeaker) {
        this.guestSpeaker = guestSpeaker;
    }

    public String getRegisterLink() {
        return registerLink;
    }

    public void setRegisterLink(String registerLink) {
        this.registerLink = registerLink;
    }

    public String getLocalImagePath() {
        return localImagePath;
    }

    public void setLocalImagePath(String localImagePath) {
        this.localImagePath = localImagePath;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
