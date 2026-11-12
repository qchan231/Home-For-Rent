package com.example.homeforrent;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.homeforrent.network.ProfileResponse;
import com.example.homeforrent.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Call<ProfileResponse> profileCall;
    private Dialog profileDialog;
    private boolean homeReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            bars.left, bars.top, bars.right, bars.bottom
                    );
                    return insets;
                }
        );

        findViewById(R.id.navLogout)
                .setOnClickListener(v -> showLogoutDialog());

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(
                        new Intent(this, ProfileActivity.class)
                )
        );

        findViewById(R.id.btnNotify).setOnClickListener(v -> {
            if (!homeReady) return;
            startActivity(
                    new Intent(this, NotificationActivity.class)
            );
        });

        findViewById(R.id.btnFeedback).setOnClickListener(v -> {
            if (!homeReady) return;
            startActivity(
                    new Intent(this, FeedbackActivity.class)
            );
        });

        findViewById(R.id.btnStatistics).setOnClickListener(v -> {
            if (!homeReady) return;

            Intent intent = new Intent(
                    this, StatisticsActivity.class
            );
            intent.putExtra(
                    StatisticsActivity.EXTRA_ROOM_NAME,
                    "Phòng 306"
            );
            intent.putExtra(
                    StatisticsActivity.EXTRA_HOUSE_NAME,
                    "Nhà trọ Thiện Kim"
            );
            startActivity(intent);
        });

        findViewById(R.id.btnInvoice).setOnClickListener(v -> {
            if (!homeReady) return;
            startActivity(
                    new Intent(this, InvoiceActivity.class)
            );
        });

        setHomeReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkProfile();
    }

    private void setHomeReady(boolean ready) {
        homeReady = ready;

        int[] featureIds = {
                R.id.btnNotify,
                R.id.btnFeedback,
                R.id.btnStatistics,
                R.id.btnInvoice
        };

        for (int id : featureIds) {
            findViewById(id).setEnabled(ready);
        }
    }

    private void checkProfile() {
        dismissProfileDialog();

        if (profileCall != null) {
            profileCall.cancel();
        }

        setHomeReady(false);

        String token = getSharedPreferences(
                "LoginPreferences", MODE_PRIVATE
        ).getString("token", "");

        if (token == null || token.trim().isEmpty()) {
            logout();
            return;
        }

        profileCall = RetrofitClient.getApiService()
                .getProfile("Bearer " + token);

        profileCall.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(
                    Call<ProfileResponse> call,
                    Response<ProfileResponse> response
            ) {
                if (call.isCanceled()
                        || isFinishing()
                        || isDestroyed()) return;

                if (response.code() == 401) {
                    logout();
                    return;
                }

                ProfileResponse body = response.body();

                if (response.isSuccessful()
                        && body != null
                        && body.isSuccess()
                        && body.getUser() != null) {

                    if (body.isProfileComplete()) {
                        setHomeReady(true);
                    } else {
                        showCompleteProfileDialog(body);
                    }
                } else {
                    showProfileCheckError();
                }
            }

            @Override
            public void onFailure(
                    Call<ProfileResponse> call,
                    Throwable t
            ) {
                if (call.isCanceled()
                        || isFinishing()
                        || isDestroyed()) return;

                showProfileCheckError();
            }
        });
    }

    private void showCompleteProfileDialog(
            ProfileResponse profile
    ) {
        String message =
                "Vui lòng hoàn thiện hồ sơ trước khi sử dụng ứng dụng.";

        if (profile.getMissingFields() != null
                && !profile.getMissingFields().isEmpty()) {
            message += "\n\nThông tin còn thiếu: "
                    + TextUtils.join(
                    ", ", profile.getMissingFields()
            );
        }

        showProfileDialog(
                "Hoàn thiện hồ sơ",
                message,
                "Cập nhật hồ sơ",
                () -> {
                    Intent intent = new Intent(
                            MainActivity.this,
                            ProfileActivity.class
                    );
                    intent.putExtra(
                            "requireProfileUpdate", true
                    );
                    startActivity(intent);
                }
        );
    }

    private void showProfileCheckError() {
        showProfileDialog(
                "Chưa kiểm tra được hồ sơ",
                "Vui lòng kiểm tra kết nối và thử lại.",
                "Thử lại",
                this::checkProfile
        );
    }

    private void showProfileDialog(
            String title,
            String message,
            String buttonText,
            Runnable action
    ) {
        dismissProfileDialog();

        // Dùng lại layout dialog_message của bạn.
        profileDialog = new Dialog(this);
        profileDialog.requestWindowFeature(
                android.view.Window.FEATURE_NO_TITLE
        );
        profileDialog.setContentView(R.layout.dialog_message);
        profileDialog.setCancelable(false);
        profileDialog.setCanceledOnTouchOutside(false);

        android.widget.ImageView icon =
                profileDialog.findViewById(R.id.imgIcon);
        android.widget.TextView titleView =
                profileDialog.findViewById(R.id.tvTitle);
        android.widget.TextView messageView =
                profileDialog.findViewById(R.id.tvMessage);
        android.widget.Button actionButton =
                profileDialog.findViewById(R.id.btnAction);

        icon.setImageResource(R.drawable.ic_user);
        titleView.setText(title);
        messageView.setText(message);
        actionButton.setText(buttonText);

        profileDialog.findViewById(R.id.btnCancel)
                .setVisibility(android.view.View.GONE);

        actionButton.setOnClickListener(v -> {
            dismissProfileDialog();
            action.run();
        });

        profileDialog.show();

        android.view.Window window = profileDialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawableResource(
                    android.R.color.transparent
            );
            window.setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void dismissProfileDialog() {
        if (profileDialog != null) {
            profileDialog.dismiss();
            profileDialog = null;
        }
    }

    private void showLogoutDialog() {
        DialogHelper.show(
                this,
                R.drawable.ic_dialog_logout,
                "Đăng xuất",
                "Bạn có chắc chắn muốn đăng xuất không?",
                "Đồng ý",
                "Hủy",
                this::logout
        );
    }

    private void logout() {
        if (profileCall != null) {
            profileCall.cancel();
        }

        dismissProfileDialog();

        getSharedPreferences("LoginPreferences", MODE_PRIVATE)
                .edit()
                .remove("token")
                .remove("userId")
                .remove("fullName")
                .remove("userEmail")
                .remove("phone")
                .remove("role")
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        if (profileCall != null) {
            profileCall.cancel();
        }

        dismissProfileDialog();
        setHomeReady(false);

        super.onPause();
    }
}