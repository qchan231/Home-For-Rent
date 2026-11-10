package com.example.homeforrent;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.homeforrent.network.ApiService;
import com.example.homeforrent.network.AuthResponse;
import com.example.homeforrent.network.ForgotPasswordRequest;
import com.example.homeforrent.network.ResetPasswordRequest;
import com.example.homeforrent.network.RetrofitClient;
import com.example.homeforrent.network.VerifyOtpRequest;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ForgotPassword extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private android.view.View stepBar1, stepBar2, stepBar3;
    private ImageView btnBack;

    // BƯỚC 1
    private EditText etEmail;
    private Button btnStep1Next;

    // BƯỚC 2
    private TextView tvTargetEmail;
    private EditText[] otpInputs = new EditText[6];
    private Button btnStep2Next;
    private TextView btnResendOtp;

    // BƯỚC 3
    private EditText etNewPassword, etConfirmPassword;
    private ImageView btnTogglePass1, btnTogglePass2;
    private Button btnStep3Submit;
    private boolean isPass1Visible = false, isPass2Visible = false;

    // ApiService Retrofit
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo Retrofit API Service
        apiService = RetrofitClient.getApiService();

        // Xử lý EdgeToEdge
        android.view.View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 1. Ánh xạ các View
        initViews();

        // 2. Lấy Email từ LoginActivity truyền sang (nếu có)
        String inputEmail = getIntent().getStringExtra("email_input");
        if (inputEmail != null && !inputEmail.isEmpty()) {
            etEmail.setText(inputEmail);
        }

        // 3. Cấu hình logic các bước
        setupStep1();
        setupOtpAutoNext();
        setupStep2();
        setupStep3();

        // Nút quay lại (Back trên giao diện)
        btnBack.setOnClickListener(v -> handleBackNavigation());

        // Xử lý nút Back hệ thống
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        stepBar1 = findViewById(R.id.stepBar1);
        stepBar2 = findViewById(R.id.stepBar2);
        stepBar3 = findViewById(R.id.stepBar3);
        btnBack = findViewById(R.id.btnBack);

        // Step 1
        etEmail = findViewById(R.id.etEmail);
        btnStep1Next = findViewById(R.id.btnStep1Next);

        // Step 2
        tvTargetEmail = findViewById(R.id.tvTargetEmail);
        otpInputs[0] = findViewById(R.id.otp1);
        otpInputs[1] = findViewById(R.id.otp2);
        otpInputs[2] = findViewById(R.id.otp3);
        otpInputs[3] = findViewById(R.id.otp4);
        otpInputs[4] = findViewById(R.id.otp5);
        otpInputs[5] = findViewById(R.id.otp6);
        btnStep2Next = findViewById(R.id.btnStep2Next);
        btnResendOtp = findViewById(R.id.btnResendOtp);

        // Step 3
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnTogglePass1 = findViewById(R.id.btnTogglePass1);
        btnTogglePass2 = findViewById(R.id.btnTogglePass2);
        btnStep3Submit = findViewById(R.id.btnStep3Submit);
    }

    private void updateStepBars(int step) {
        stepBar1.setBackgroundResource(step >= 1 ? R.drawable.bg_step_active : R.drawable.bg_step_inactive);
        stepBar2.setBackgroundResource(step >= 2 ? R.drawable.bg_step_active : R.drawable.bg_step_inactive);
        stepBar3.setBackgroundResource(step >= 3 ? R.drawable.bg_step_active : R.drawable.bg_step_inactive);
    }

    // --- BƯỚC 1 LOGIC: Gửi mã OTP ---
    private void setupStep1() {
        btnStep1Next.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Vui lòng nhập đúng định dạng Email");
                etEmail.requestFocus();
                return;
            }

            btnStep1Next.setEnabled(false);
            apiService.forgotPassword(new ForgotPasswordRequest(email)).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    btnStep1Next.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPassword.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        tvTargetEmail.setText("Mã gồm 6 chữ số đã được gửi tới " + email);
                        viewFlipper.setDisplayedChild(1);
                        updateStepBars(2);
                    } else if (response.code() == 404) {
                        showEmailNotFoundDialog();
                    } else {
                        Toast.makeText(ForgotPassword.this, getErrorMessage(response), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    btnStep1Next.setEnabled(true);
                    Toast.makeText(ForgotPassword.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // --- BƯỚC 2 LOGIC: Tự động di chuyển con trỏ ô OTP ---
    private void setupOtpAutoNext() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) {
                        otpInputs[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpInputs[index].getText().toString().isEmpty() && index > 0) {
                        otpInputs[index - 1].requestFocus();
                    }
                }
                return false;
            });
        }
    }

    // --- BƯỚC 2 LOGIC: Xác nhận OTP & Gửi lại OTP ---
    private void setupStep2() {
        // Gửi lại mã OTP
        btnResendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            btnResendOtp.setEnabled(false);
            apiService.forgotPassword(new ForgotPasswordRequest(email)).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    btnResendOtp.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPassword.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPassword.this, getErrorMessage(response), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    btnResendOtp.setEnabled(true);
                    Toast.makeText(ForgotPassword.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Xác nhận OTP
        btnStep2Next.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            StringBuilder otpBuilder = new StringBuilder();
            for (EditText input : otpInputs) {
                otpBuilder.append(input.getText().toString().trim());
            }
            String otp = otpBuilder.toString();

            if (otp.length() < 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            btnStep2Next.setEnabled(false);
            apiService.verifyOtp(new VerifyOtpRequest(email, otp)).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    btnStep2Next.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPassword.this, "Mã OTP hợp lệ!", Toast.LENGTH_SHORT).show();
                        viewFlipper.setDisplayedChild(2);
                        updateStepBars(3);
                    } else if (response.code() == 400 || response.code() == 401) {
                        showInvalidOtpDialog();
                    } else {
                        Toast.makeText(ForgotPassword.this, getErrorMessage(response), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    btnStep2Next.setEnabled(true);
                    Toast.makeText(ForgotPassword.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // --- BƯỚC 3 LOGIC: Đặt lại mật khẩu mới ---
    private void setupStep3() {
        btnTogglePass1.setOnClickListener(v -> {
            isPass1Visible = !isPass1Visible;
            togglePasswordVisibility(etNewPassword, isPass1Visible);
        });

        btnTogglePass2.setOnClickListener(v -> {
            isPass2Visible = !isPass2Visible;
            togglePasswordVisibility(etConfirmPassword, isPass2Visible);
        });

        btnStep3Submit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            StringBuilder otpBuilder = new StringBuilder();
            for (EditText input : otpInputs) {
                otpBuilder.append(input.getText().toString().trim());
            }
            String otp = otpBuilder.toString();
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (newPass.length() < 6) {
                etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                etNewPassword.requestFocus();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                etConfirmPassword.requestFocus();
                return;
            }

            btnStep3Submit.setEnabled(false);
            apiService.resetPassword(new ResetPasswordRequest(email, otp, newPass)).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    btnStep3Submit.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        showSuccessDialog();
                    } else {
                        Toast.makeText(ForgotPassword.this, getErrorMessage(response), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    btnStep3Submit.setEnabled(true);
                    Toast.makeText(ForgotPassword.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ================== CÁC DIALOG (dùng DialogHelper) ==================

    private void showSuccessDialog() {
        DialogHelper.show(
                this,
                R.drawable.ic_check_success,
                "Đổi mật khẩu thành công!",
                "Bạn có thể đăng nhập bằng mật khẩu mới.",
                "Trở lại đăng nhập   ➜",
                () -> {
                    Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    private void showEmailNotFoundDialog() {
        DialogHelper.show(
                this,
                R.drawable.ic_error_email,
                "Email không tồn tại",
                "Chúng tôi không tìm thấy tài khoản nào dùng email này. Vui lòng kiểm tra lại.",
                "Nhập lại email",
                () -> {
                    etEmail.requestFocus();
                    etEmail.selectAll();
                });
    }

    private void showInvalidOtpDialog() {
        DialogHelper.show(
                this,
                R.drawable.ic_error_otp,
                "Mã OTP không hợp lệ",
                "Mã bạn nhập sai hoặc đã hết hạn. Vui lòng thử lại hoặc yêu cầu gửi mã mới.",
                "Nhập lại mã",
                () -> {
                    for (EditText input : otpInputs) input.setText("");
                    otpInputs[0].requestFocus();
                });
    }

    // ====================================================================

    private void togglePasswordVisibility(EditText editText, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editText.setSelection(editText.getText().length());
    }

    private void handleBackNavigation() {
        int currentStep = viewFlipper.getDisplayedChild();
        if (currentStep > 0) {
            viewFlipper.setDisplayedChild(currentStep - 1);
            updateStepBars(currentStep);
        } else {
            finish();
        }
    }

    // Trích xuất thông báo lỗi từ response errorBody khi HTTP status code != 2xx
    private String getErrorMessage(Response<AuthResponse> response) {
        try {
            if (response.errorBody() != null) {
                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                return jsonObject.optString("message", "Đã có lỗi xảy ra!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Lỗi (" + response.code() + ")";
    }
}