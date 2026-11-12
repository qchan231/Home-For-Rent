package com.example.homeforrent.network;

public class ChangePasswordRequest {
    private final String currentPassword;
    private final String newPassword;

    public ChangePasswordRequest(
            String currentPassword,
            String newPassword
    ) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}