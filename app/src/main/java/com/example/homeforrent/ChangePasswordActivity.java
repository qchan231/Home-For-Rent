package com.example.homeforrent;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.homeforrent.network.AuthResponse;
import com.example.homeforrent.network.ChangePasswordRequest;
import com.example.homeforrent.network.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnChangePassword;

    private boolean isSaving = false;
    private Call<AuthResponse> changePasswordCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

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

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        setupPasswordToggle(
                etCurrentPassword,
                findViewById(R.id.btnToggleCurrentPassword),
                "mật khẩu hiện tại"
        );

        setupPasswordToggle(
                etNewPassword,
                findViewById(R.id.btnToggleNewPassword),
                "mật khẩu mới"
        );

        setupPasswordToggle(
                etConfirmPassword,
                findViewById(R.id.btnToggleConfirmPassword),
                "xác nhận mật khẩu mới"
        );

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnChangePassword.setOnClickListener(v -> changePassword());

        etConfirmPassword.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId
                            == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                        changePassword();
                        return true;
                    }
                    return false;
                }
        );
    }

    private void setupPasswordToggle(
            EditText field,
            ImageView button,
            String description
    ) {
        button.setOnClickListener(v -> {
            int cursor = field.getSelectionStart();

            boolean currentlyHidden =
                    field.getTransformationMethod()
                            instanceof PasswordTransformationMethod;

            field.setTransformationMethod(
                    currentlyHidden
                            ? HideReturnsTransformationMethod.getInstance()
                            : PasswordTransformationMethod.getInstance()
            );

            button.setContentDescription(
                    (currentlyHidden ? "Ẩn " : "Hiện ") + description
            );

            field.setSelection(
                    Math.max(0, Math.min(cursor, field.length()))
            );
        });
    }

    private void changePassword() {
        if (isSaving) return;

        // Không trim mật khẩu: khoảng trắng có thể là một phần mật khẩu.
        String currentPassword =
                etCurrentPassword.getText().toString();
        String newPassword =
                etNewPassword.getText().toString();
        String confirmPassword =
                etConfirmPassword.getText().toString();

        if (currentPassword.isEmpty()) {
            showFieldError(
                    etCurrentPassword,
                    "Vui lòng nhập mật khẩu hiện tại"
            );
            return;
        }

        if (newPassword.length() < 6) {
            showFieldError(
                    etNewPassword,
                    "Mật khẩu mới phải có ít nhất 6 ký tự"
            );
            return;
        }

        if (newPassword.equals(currentPassword)) {
            showFieldError(
                    etNewPassword,
                    "Mật khẩu mới phải khác mật khẩu hiện tại"
            );
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showFieldError(
                    etConfirmPassword,
                    "Xác nhận mật khẩu không khớp"
            );
            return;
        }

        String token = getSharedPreferences(
                "LoginPreferences", MODE_PRIVATE
        ).getString("token", "");

        if (token == null || token.trim().isEmpty()) {
            goToLogin();
            return;
        }

        setSaving(true);

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        currentPassword,
                        newPassword
                );

        changePasswordCall = RetrofitClient.getApiService()
                .changePassword("Bearer " + token, request);

        changePasswordCall.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    Call<AuthResponse> call,
                    Response<AuthResponse> response
            ) {
                if (call.isCanceled()
                        || isFinishing()
                        || isDestroyed()) return;

                if (response.code() == 401) {
                    Toast.makeText(
                            ChangePasswordActivity.this,
                            "Phiên đăng nhập đã hết hạn",
                            Toast.LENGTH_SHORT
                    ).show();
                    goToLogin();
                    return;
                }

                AuthResponse body = response.body();

                if (response.isSuccessful()
                        && body != null
                        && body.isSuccess()) {

                    // Ngăn gửi lại trong lúc dialog thành công đang mở.
                    etCurrentPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");

                    DialogHelper.show(
                            ChangePasswordActivity.this,
                            R.drawable.ic_lock,
                            "Đổi mật khẩu thành công",
                            "Mật khẩu của bạn đã được cập nhật.",
                            "Về hồ sơ",
                            () -> finish()
                    );
                } else {
                    setSaving(false);

                    Toast.makeText(
                            ChangePasswordActivity.this,
                            errorMessage(response),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<AuthResponse> call,
                    Throwable t
            ) {
                if (call.isCanceled()
                        || isFinishing()
                        || isDestroyed()) return;

                setSaving(false);

                Toast.makeText(
                        ChangePasswordActivity.this,
                        "Không nhận được kết quả từ máy chủ. "
                                + "Nếu mật khẩu đã đổi, hãy đăng nhập bằng mật khẩu mới.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showFieldError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }

    private void setSaving(boolean saving) {
        isSaving = saving;

        etCurrentPassword.setEnabled(!saving);
        etNewPassword.setEnabled(!saving);
        etConfirmPassword.setEnabled(!saving);
        btnChangePassword.setEnabled(!saving);

        btnChangePassword.setText(
                saving ? "Đang cập nhật..." : "Đổi mật khẩu"
        );
    }

    private String errorMessage(Response<AuthResponse> response) {
        try {
            if (response.errorBody() != null) {
                AuthResponse error = new Gson().fromJson(
                        response.errorBody().string(),
                        AuthResponse.class
                );

                if (error != null && error.getMessage() != null) {
                    return error.getMessage();
                }
            }
        } catch (Exception ignored) {
        }

        return "Không đổi được mật khẩu";
    }

    private void goToLogin() {
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
    protected void onDestroy() {
        if (changePasswordCall != null) {
            changePasswordCall.cancel();
        }
        super.onDestroy();
    }
}