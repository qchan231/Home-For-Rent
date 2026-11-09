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

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName;
    private EditText edtPhone;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    private CheckBox cbTerms;

    private TextView btnShowPassword;
    private TextView btnShowConfirmPassword;

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

        btnShowPassword = findViewById(
                R.id.btnShowRegisterPassword
        );

        btnShowConfirmPassword = findViewById(
                R.id.btnShowConfirmPassword
        );

        TextView btnCreate = findViewById(
                R.id.btnCreateAccount
        );

        TextView btnBack = findViewById(
                R.id.btnBack
        );

        TextView btnGoToLogin = findViewById(
                R.id.btnGoToLogin
        );

        // QUAY LẠI

        btnBack.setOnClickListener(v -> finish());

        btnGoToLogin.setOnClickListener(v -> finish());

        // HIỆN / ẨN MẬT KHẨU

        btnShowPassword.setOnClickListener(v -> {
            togglePassword();
        });

        btnShowConfirmPassword.setOnClickListener(v -> {
            toggleConfirmPassword();
        });

        // ĐĂNG KÝ

        btnCreate.setOnClickListener(v -> {
            register();
        });
    }

    // HIỆN / ẨN MẬT KHẨU

    private void togglePassword() {

        passwordVisible = !passwordVisible;

        int cursor = edtPassword.getSelectionStart();

        if (passwordVisible) {

            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );

            btnShowPassword.setText("⊘");

        } else {

            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
            );

            btnShowPassword.setText("◉");
        }

        edtPassword.setSelection(
                Math.max(0, Math.min(cursor,
                        edtPassword.length()))
        );
    }

    private void toggleConfirmPassword() {

        confirmPasswordVisible = !confirmPasswordVisible;

        int cursor = edtConfirmPassword.getSelectionStart();

        if (confirmPasswordVisible) {

            edtConfirmPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );

            btnShowConfirmPassword.setText("⊘");

        } else {

            edtConfirmPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
            );

            btnShowConfirmPassword.setText("◉");
        }

        edtConfirmPassword.setSelection(
                Math.max(0, Math.min(cursor,
                        edtConfirmPassword.length()))
        );
    }

    // XỬ LÝ ĐĂNG KÝ

    private void register() {

        String name = edtName.getText()
                .toString().trim();

        String phone = edtPhone.getText()
                .toString().trim();

        String email = edtEmail.getText()
                .toString().trim();

        String password = edtPassword.getText()
                .toString();

        String confirmPassword = edtConfirmPassword.getText()
                .toString();

        // HỌ TÊN

        if (name.isEmpty()) {

            edtName.setError("Vui lòng nhập họ tên");
            edtName.requestFocus();
            return;
        }

        // SỐ ĐIỆN THOẠI

        if (!phone.matches("^0[0-9]{9,10}$")) {

            edtPhone.setError(
                    "Số điện thoại không hợp lệ"
            );

            edtPhone.requestFocus();
            return;
        }

        // EMAIL

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        // MẬT KHẨU

        if (password.length() < 6) {

            edtPassword.setError(
                    "Mật khẩu phải có ít nhất 6 ký tự"
            );

            edtPassword.requestFocus();
            return;
        }

        // XÁC NHẬN MẬT KHẨU

        if (!password.equals(confirmPassword)) {

            edtConfirmPassword.setError(
                    "Mật khẩu xác nhận không khớp"
            );

            edtConfirmPassword.requestFocus();
            return;
        }

        // ĐIỀU KHOẢN

        if (!cbTerms.isChecked()) {

            Toast.makeText(
                    this,
                    "Bạn cần đồng ý với điều khoản sử dụng!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * ĐÂY LÀ BƯỚC DEMO.
         * Chưa lưu tài khoản vào cơ sở dữ liệu.
         * Sau này sẽ kết nối Firebase hoặc Backend.
         */

        Toast.makeText(
                this,
                "Thông tin hợp lệ! Đăng ký thành công (demo).",
                Toast.LENGTH_LONG
        ).show();

        // QUAY VỀ LOGIN

        Intent intent = new Intent(
                RegisterActivity.this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);

        finish();
    }
}