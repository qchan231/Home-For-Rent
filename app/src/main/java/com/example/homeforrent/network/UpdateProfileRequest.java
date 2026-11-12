package com.example.homeforrent.network;

public class UpdateProfileRequest {
    private final String fullName;
    private final String phone;
    private final String dateOfBirth;
    private final String hometown;
    private final String currentAddress;
    private final String citizenId;

    public UpdateProfileRequest(
            String fullName,
            String phone,
            String dateOfBirth,
            String hometown,
            String currentAddress,
            String citizenId
    ) {
        this.fullName = fullName;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.hometown = hometown;
        this.currentAddress = currentAddress;
        this.citizenId = citizenId;
    }
}