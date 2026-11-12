package com.example.homeforrent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.homeforrent.network.ProfileResponse;
import com.example.homeforrent.network.RetrofitClient;
import com.example.homeforrent.network.UpdateProfileRequest;
import com.example.homeforrent.network.UserData;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtBirthday, edtHometown;
    private EditText edtAddress, edtCccd, edtEmail, edtPhone;
    private TextView btnEdit, tvAvatar;

    private SharedPreferences preferences;
    private boolean isEditing = false;
    private boolean isBusy = false;
    private boolean profileLoaded = false;

    private Call<ProfileResponse> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

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

        preferences = getSharedPreferences(
                "LoginPreferences", MODE_PRIVATE
        );

        edtFullName = findViewById(R.id.edtFullName);
        edtBirthday = findViewById(R.id.edtBirthday);
        edtHometown = findViewById(R.id.edtHometown);
        edtAddress = findViewById(R.id.edtAddress);
        edtCccd = findViewById(R.id.edtCccd);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnEdit = findViewById(R.id.btnEdit);
        tvAvatar = findViewById(R.id.tvAvatar);

        // Xóa dữ liệu mẫu trong XML.
        EditText[] fields = {
                edtFullName, edtBirthday, edtHometown,
                edtAddress, edtCccd, edtEmail, edtPhone
        };

        for (EditText field : fields) {
            field.setText("");
        }

        edtBirthday.setHint("dd/MM/yyyy");
        updateAvatar();
        setEditing(false);

        btnEdit.setOnClickListener(v -> {
            if (isBusy) return;

            if (!profileLoaded) {
                loadProfile();
            } else if (!isEditing) {
                setEditing(true);
            } else {
                saveProfile();
            }
        });

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());

        findViewById(R.id.btnChangePassword).setOnClickListener(v ->
                startActivity(
                        new Intent(
                                ProfileActivity.this,
                                ChangePasswordActivity.class
                        )
                )
        );

        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navLogout)
                .setOnClickListener(v -> showLogoutDialog());

        loadProfile();
    }

    private String authorization() {
        String token = preferences.getString("token", "");

        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        return "Bearer " + token;
    }

    private void loadProfile() {
        String header = authorization();

        if (header == null) {
            logout();
            return;
        }

        setBusy(true);

        activeCall = RetrofitClient.getApiService().getProfile(header);
        activeCall.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(
                    Call<ProfileResponse> call,
                    Response<ProfileResponse> response
            ) {
                if (isFinishing() || isDestroyed()) return;

                if (response.code() == 401) {
                    logout();
                    return;
                }

                ProfileResponse body = response.body();

                if (response.isSuccessful()
                        && body != null
                        && body.isSuccess()
                        && body.getUser() != null) {

                    showProfile(body.getUser());
                    profileLoaded = true;
                    setBusy(false);

                    boolean requireUpdate = getIntent()
                            .getBooleanExtra(
                                    "requireProfileUpdate", false
                            );

                    setEditing(
                            requireUpdate || !body.isProfileComplete()
                    );
                } else {
                    setBusy(false);
                    Toast.makeText(
                            ProfileActivity.this,
                            errorMessage(response, "Không tải được hồ sơ"),
                            Toast.LENGTH_LONG
                    ).show();
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

                setBusy(false);

                Toast.makeText(
                        ProfileActivity.this,
                        "Không kết nối được máy chủ. Nhấn Thử lại.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showProfile(UserData user) {
        edtFullName.setText(user.getFullName());
        edtEmail.setText(user.getEmail());
        edtPhone.setText(user.getPhone());
        edtHometown.setText(user.getHometown());
        edtAddress.setText(user.getCurrentAddress());
        edtCccd.setText(user.getCitizenId());

        edtBirthday.setText(
                apiDateToDisplay(user.getDateOfBirth())
        );

        // Đồng bộ thông tin mà các màn hình khác đang sử dụng.
        preferences.edit()
                .putString("userId", user.getId())
                .putString("fullName", user.getFullName())
                .putString("userEmail", user.getEmail())
                .putString("phone", user.getPhone())
                .putString("role", user.getRole())
                .apply();

        updateAvatar();
    }

    private void saveProfile() {
        if (!required(edtFullName, "Vui lòng nhập họ tên")) return;
        if (!required(edtPhone, "Vui lòng nhập số điện thoại")) return;
        if (!required(edtBirthday, "Vui lòng nhập ngày sinh")) return;
        if (!required(edtHometown, "Vui lòng nhập quê quán")) return;
        if (!required(edtAddress, "Vui lòng nhập nơi ở hiện tại")) return;
        if (!required(edtCccd, "Vui lòng nhập CCCD")) return;

        String phone = text(edtPhone);
        String citizenId = text(edtCccd);

        if (!phone.matches("[0-9]{10,15}")) {
            edtPhone.setError("Số điện thoại gồm 10–15 chữ số");
            edtPhone.requestFocus();
            return;
        }

        if (!citizenId.matches("[0-9]{12}")) {
            edtCccd.setError("CCCD phải gồm 12 chữ số");
            edtCccd.requestFocus();
            return;
        }

        String birthday = displayDateToApi(text(edtBirthday));

        if (birthday == null) {
            edtBirthday.setError(
                    "Nhập ngày hợp lệ theo dạng dd/MM/yyyy, không ở tương lai"
            );
            edtBirthday.requestFocus();
            return;
        }

        String header = authorization();

        if (header == null) {
            logout();
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(
                text(edtFullName),
                phone,
                birthday,
                text(edtHometown),
                text(edtAddress),
                citizenId
        );

        setBusy(true);

        activeCall = RetrofitClient.getApiService()
                .updateProfile(header, request);

        activeCall.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(
                    Call<ProfileResponse> call,
                    Response<ProfileResponse> response
            ) {
                if (isFinishing() || isDestroyed()) return;

                if (response.code() == 401) {
                    logout();
                    return;
                }

                ProfileResponse body = response.body();

                if (response.isSuccessful()
                        && body != null
                        && body.isSuccess()
                        && body.getUser() != null) {

                    showProfile(body.getUser());
                    setBusy(false);
                    setEditing(!body.isProfileComplete());

                    Toast.makeText(
                            ProfileActivity.this,
                            "Đã lưu hồ sơ vào hệ thống",
                            Toast.LENGTH_SHORT
                    ).show();

                    // Nếu Home yêu cầu cập nhật, trở về để kiểm tra lại.
                    if (body.isProfileComplete()
                            && getIntent().getBooleanExtra(
                            "requireProfileUpdate", false
                    )) {
                        finish();
                    }
                } else {
                    setBusy(false);

                    Toast.makeText(
                            ProfileActivity.this,
                            errorMessage(response, "Không lưu được hồ sơ"),
                            Toast.LENGTH_LONG
                    ).show();
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

                setBusy(false);

                Toast.makeText(
                        ProfileActivity.this,
                        "Mất kết nối. Hãy thử lưu lại.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private boolean required(EditText field, String message) {
        if (text(field).isEmpty()) {
            field.setError(message);
            field.requestFocus();
            return false;
        }
        return true;
    }

    private String text(EditText field) {
        return field.getText().toString().trim();
    }

    private void setEditing(boolean editing) {
        isEditing = editing;
        refreshControls();
    }

    private void setBusy(boolean busy) {
        isBusy = busy;
        refreshControls();
    }

    private void refreshControls() {
        EditText[] fields = {
                edtFullName, edtBirthday, edtHometown,
                edtAddress, edtCccd, edtPhone
        };

        for (EditText field : fields) {
            field.setEnabled(isEditing && !isBusy);
        }

        // Email đăng nhập không được sửa qua API hồ sơ.
        edtEmail.setEnabled(false);

        btnEdit.setEnabled(!isBusy);

        if (isBusy) {
            btnEdit.setText("Đang xử lý...");
        } else if (!profileLoaded) {
            btnEdit.setText("Thử lại");
        } else {
            btnEdit.setText(isEditing ? "Lưu" : "Chỉnh sửa");
        }
    }

    private SimpleDateFormat dateFormat(String pattern) {
        SimpleDateFormat format =
                new SimpleDateFormat(pattern, Locale.US);
        format.setLenient(false);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format;
    }

    private String apiDateToDisplay(String value) {
        if (value == null || value.isEmpty()) return "";

        try {
            Date date = dateFormat("yyyy-MM-dd").parse(value);
            return date == null
                    ? ""
                    : dateFormat("dd/MM/yyyy").format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    private String displayDateToApi(String value) {
        if (!value.matches("\\d{2}/\\d{2}/\\d{4}")) return null;

        try {
            SimpleDateFormat display = dateFormat("dd/MM/yyyy");
            Date date = display.parse(value);

            if (date == null
                    || !display.format(date).equals(value)
                    || date.after(new Date())) {
                return null;
            }

            String result = dateFormat("yyyy-MM-dd").format(date);

            return result.matches("\\d{4}-\\d{2}-\\d{2}")
                    ? result
                    : null;
        } catch (ParseException e) {
            return null;
        }
    }

    private String errorMessage(
            Response<ProfileResponse> response,
            String fallback
    ) {
        try {
            if (response.errorBody() != null) {
                ProfileResponse error = new Gson().fromJson(
                        response.errorBody().string(),
                        ProfileResponse.class
                );

                if (error != null && error.getMessage() != null) {
                    return error.getMessage();
                }
            }
        } catch (Exception ignored) {
        }

        return fallback;
    }

    private void updateAvatar() {
        String name = text(edtFullName);

        if (name.isEmpty()) {
            tvAvatar.setText("?");
            return;
        }

        String[] parts = name.split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1
                ? parts[parts.length - 1].substring(0, 1)
                : "";

        tvAvatar.setText(
                (first + last).toUpperCase(Locale.getDefault())
        );
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
        if (activeCall != null) {
            activeCall.cancel();
        }

        preferences.edit()
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
        if (activeCall != null) {
            activeCall.cancel();
        }
        super.onDestroy();
    }
}