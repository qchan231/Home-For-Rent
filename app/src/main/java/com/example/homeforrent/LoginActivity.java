package com.example.homeforrent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.homeforrent.network.AuthResponse;
import com.example.homeforrent.network.LoginRequest;
import com.example.homeforrent.network.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;


public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private CheckBox cbRemember;
    private TextView btnLogin;
    private ImageView btnShowPassword;
    private boolean isPasswordVisible = false;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ÁNH XẠ VIEW
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        cbRemember = findViewById(R.id.cbRemember);
        btnLogin = findViewById(R.id.btnLogin);
        btnShowPassword = findViewById(R.id.btnShowPassword);
        TextView btnForgotPassword = findViewById(R.id.btnForgotPassword);
        TextView btnRegister = findViewById(R.id.btnRegister);

        // SHARED PREFERENCES
        preferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE);
        loadRememberedEmail();

        // HIỆN / ẨN MẬT KHẨU
        btnShowPassword.setOnClickListener(v -> togglePassword());

        // ĐĂNG NHẬP
        btnLogin.setOnClickListener(v -> login());

        // QUÊN MẬT KHẨU
        btnForgotPassword.setOnClickListener(v -> forgotPassword());

        // ĐĂNG KÝ
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // GHI NHỚ EMAIL
    private void loadRememberedEmail() {
        String email = preferences.getString("email", "");
        boolean remember = preferences.getBoolean("remember", false);

        if (remember) {
            edtEmail.setText(email);
            cbRemember.setChecked(true);
        }
    }

    // HIỆN / ẨN MẬT KHẨU
    private void togglePassword() {
        int cursorPosition = edtPassword.getSelectionStart();

        if (isPasswordVisible) {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            btnShowPassword.setContentDescription("Hiện mật khẩu");
            isPasswordVisible = false;
        } else {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            btnShowPassword.setContentDescription("Ẩn mật khẩu");
            isPasswordVisible = true;
        }

        edtPassword.setSelection(Math.max(0, cursorPosition));
    }

    // XỬ LÝ ĐĂNG NHẬP
    private void login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString(); // không trim, giống lúc đăng ký

        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return;
        }

        // Chặn bấm nhiều lần
        btnLogin.setEnabled(false);

        RetrofitClient.getApiService().login(new LoginRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnLogin.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            AuthResponse auth = response.body();

                            // Ghi nhớ email
                            SharedPreferences.Editor editor = preferences.edit();
                            if (cbRemember.isChecked()) {
                                editor.putString("email", email);
                                editor.putBoolean("remember", true);
                            } else {
                                editor.remove("email");
                                editor.putBoolean("remember", false);
                            }
                            // Lưu token + thông tin user
                            editor.putString("token", auth.getToken());
                            if (auth.getUser() != null) {
                                editor.putString("userId", auth.getUser().getId());
                                editor.putString("fullName", auth.getUser().getFullName());
                                editor.putString("userEmail", auth.getUser().getEmail());
                                editor.putString("phone", auth.getUser().getPhone());
                                editor.putString("role", auth.getUser().getRole());
                            }
                            editor.apply();

                            Toast.makeText(LoginActivity.this, auth.getMessage(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Server trả 400: "Email hoặc mật khẩu không đúng!"
                            String msg = "Đăng nhập thất bại";
                            try {
                                if (response.errorBody() != null) {
                                    AuthResponse err = new Gson().fromJson(
                                            response.errorBody().string(), AuthResponse.class);
                                    if (err != null && err.getMessage() != null) {
                                        msg = err.getMessage();
                                    }
                                }
                            } catch (Exception ignored) {}
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        android.util.Log.e("LOGIN", "Lỗi kết nối", t);
                        Toast.makeText(LoginActivity.this,
                                "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // QUÊN MẬT KHẨU (Chuyển sang ForgotPasswordActivity)
    private void forgotPassword() {
        Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);

        // Lấy email đã nhập (nếu có) truyền sang Bước 1
        String email = edtEmail.getText().toString().trim();
        if (!email.isEmpty()) {
            intent.putExtra("email_input", email);
        }

        startActivity(intent);
    }
}