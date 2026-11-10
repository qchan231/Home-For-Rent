package com.example.homeforrent.network;

public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String role;

    public RegisterRequest(String email, String password, String fullName, String phone, String role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
}