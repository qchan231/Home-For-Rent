package com.example.homeforrent.network;

import java.util.List;

public class ProfileResponse {
    private boolean success;
    private String message;
    private boolean profileComplete;
    private List<String> missingFields;
    private UserData user;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public UserData getUser() {
        return user;
    }
}