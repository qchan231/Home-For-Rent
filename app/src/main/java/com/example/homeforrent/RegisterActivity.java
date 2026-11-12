package com.example.homeforrent;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeforrent.network.AuthResponse;
import com.example.homeforrent.network.RegisterRequest;
import com.example.homeforrent.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.ImageView;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName;
    private EditText edtPhone;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    private CheckBox cbTerms;

    private ImageView btnShowPassword;
    private ImageView btnShowConfirmPassword;

    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ÁNH XẠ VIEW
        edtName = findViewById(R.id.edtRegisterName);
        edtPhone = findViewById(R.id.edtRegisterPhone);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        cbTerms = findViewById(R.id.cbTerms);

        btnShowPassword = findViewById(R.id.btnShowRegisterPassword);
        btnShowConfirmPassword = findViewById(R.id.btnShowConfirmPassword);

        TextView btnCreate = findViewById(R.id.btnCreateAccount);
        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnGoToLogin = findViewById(R.id.btnGoToLogin);

        // QUAY LẠI
        btnBack.setOnClickListener(v -> finish());
        btnGoToLogin.setOnClickListener(v -> finish());

        // HIỆN / ẨN MẬT KHẨU
        btnShowPassword.setOnClickListener(v -> togglePassword());
        btnShowConfirmPassword.setOnClickListener(v -> toggleConfirmPassword());

        // ĐĂNG KÝ
        btnCreate.setOnClickListener(v -> register());
    }

    // HIỆN / ẨN MẬT KHẨU
    private void togglePassword() {
        passwordVisible = !passwordVisible;
        int cursor = edtPassword.getSelectionStart();

        if (passwordVisible) {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            btnShowPassword.setContentDescription("Ẩn mật khẩu");
        } else {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            btnShowPassword.setContentDescription("Hiện mật khẩu");
        }

        edtPassword.setSelection(Math.max(0, Math.min(cursor, edtPassword.length())));
    }

    private void toggleConfirmPassword() {
        confirmPasswordVisible = !confirmPasswordVisible;
        int cursor = edtConfirmPassword.getSelectionStart();

        if (confirmPasswordVisible) {
            edtConfirmPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            btnShowConfirmPassword.setContentDescription("Ẩn xác nhận mật khẩu");
        } else {
            edtConfirmPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            btnShowConfirmPassword.setContentDescription("Hiện xác nhận mật khẩu");
        }

        edtConfirmPassword.setSelection(Math.max(0, Math.min(cursor, edtConfirmPassword.length())));
    }

    // XỬ LÝ ĐĂNG KÝ RETROFIT API
    private void register() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        // VALIDATION
        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập họ tên");
            edtName.requestFocus();
            return;
        }

        if (!phone.matches("^0[0-9]{9,10}$")) {
            edtPhone.setError("Số điện thoại không hợp lệ");
            edtPhone.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            edtConfirmPassword.requestFocus();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Bạn cần đồng ý với điều khoản sử dụng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // TẠO REQUEST GỬI LÊN SERVER NODE.JS
        RegisterRequest registerRequest = new RegisterRequest(email, password, name, phone, "user");

        // GỌI API QUA RETROFIT
        RetrofitClient.getApiService().register(registerRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showRegisterSuccessDialog();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại! Email có thể đã được sử dụng.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void showRegisterSuccessDialog() {
        DialogHelper.show(
                this,
                R.drawable.ic_check_success,
                "Đăng ký thành công!",
                "Tài khoản của bạn đã được khởi tạo. Vui lòng đăng nhập để tiếp tục.",
                "Đăng nhập ngay   ➜",
                () -> {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
    }
}