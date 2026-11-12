package com.example.homeforrent.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Header;


public interface ApiService {

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/forgot-password")
    Call<AuthResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/auth/verify-otp")
    Call<AuthResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("api/auth/reset-password")
    Call<AuthResponse> resetPassword(@Body ResetPasswordRequest request);

    @GET("api/users/me")
    Call<ProfileResponse> getProfile(
            @Header("Authorization") String authorization
    );

    @PATCH("api/users/me")
    Call<ProfileResponse> updateProfile(
            @Header("Authorization") String authorization,
            @Body UpdateProfileRequest request
    );
    @PATCH("api/users/me/password")
    Call<AuthResponse> changePassword(
            @Header("Authorization") String authorization,
            @Body ChangePasswordRequest request
    );
}