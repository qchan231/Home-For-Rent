package com.example.homeforrent.network;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private UserData user;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public UserData getUser() { return user; }
}