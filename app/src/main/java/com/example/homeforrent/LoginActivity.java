package com.example.homeforrent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private CheckBox cbRemember;
    private TextView btnLogin, btnShowPassword;
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

        TextView btnForgotPassword =
                findViewById(R.id.btnForgotPassword);

        TextView btnRegister =
                findViewById(R.id.btnRegister);

        // SHARED PREFERENCES

        preferences = getSharedPreferences(
                "LoginPreferences",
                MODE_PRIVATE
        );

        loadRememberedEmail();

        // HIỆN / ẨN MẬT KHẨU

        btnShowPassword.setOnClickListener(v -> {
            togglePassword();
        });

        // ĐĂNG NHẬP

        btnLogin.setOnClickListener(v -> {
            login();
        });

        // QUÊN MẬT KHẨU

        btnForgotPassword.setOnClickListener(v -> {
            forgotPassword();
        });

        // ĐĂNG KÝ

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    // GHI NHỚ EMAIL

    private void loadRememberedEmail() {

        String email = preferences.getString(
                "email",
                ""
        );

        boolean remember = preferences.getBoolean(
                "remember",
                false
        );

        if (remember) {
            edtEmail.setText(email);
            cbRemember.setChecked(true);
        }
    }

    // HIỆN / ẨN MẬT KHẨU

    private void togglePassword() {

        int cursorPosition =
                edtPassword.getSelectionStart();

        if (isPasswordVisible) {

            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
            );

            btnShowPassword.setText("◉");

            isPasswordVisible = false;

        } else {

            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );

            btnShowPassword.setText("⊘");

            isPasswordVisible = true;
        }

        edtPassword.setSelection(
                Math.max(0, cursorPosition)
        );
    }

    // XỬ LÝ ĐĂNG NHẬP

    private void login() {

        String email = edtEmail.getText()
                .toString()
                .trim();

        String password = edtPassword.getText()
                .toString()
                .trim();

        // KIỂM TRA EMAIL

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

        // KIỂM TRA MẬT KHẨU

        if (password.isEmpty()) {

            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();

            return;
        }

        if (password.length() < 6) {

            edtPassword.setError(
                    "Mật khẩu phải có ít nhất 6 ký tự"
            );

            edtPassword.requestFocus();

            return;
        }

        // LƯU EMAIL NẾU CHỌN GHI NHỚ

        SharedPreferences.Editor editor =
                preferences.edit();

        if (cbRemember.isChecked()) {

            editor.putString("email", email);
            editor.putBoolean("remember", true);

        } else {

            editor.remove("email");
            editor.putBoolean("remember", false);
        }

        editor.apply();

        /*
         * ĐÂY LÀ ĐĂNG NHẬP DEMO.
         * Khi có Backend, thay phần này bằng API xác thực.
         */

        Toast.makeText(
                this,
                "Đăng nhập thành công!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        startActivity(intent);

        finish();
    }

    // QUÊN MẬT KHẨU

    private void forgotPassword() {

        EditText input = new EditText(this);

        input.setHint("Nhập email của bạn");
        input.setSingleLine(true);
        input.setInputType(
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle("Khôi phục mật khẩu")
                .setMessage(
                        "Vui lòng nhập email để yêu cầu khôi phục mật khẩu."
                )
                .setView(input)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (dialog, which) -> {

                    String email = input.getText()
                            .toString()
                            .trim();

                    if (!Patterns.EMAIL_ADDRESS
                            .matcher(email).matches()) {

                        Toast.makeText(
                                this,
                                "Email không hợp lệ!",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Chức năng khôi phục cần kết nối máy chủ.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .show();
    }
}