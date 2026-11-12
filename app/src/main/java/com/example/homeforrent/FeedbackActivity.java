package com.example.homeforrent; //

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_HOUSE_NAME = "house_name";

    private EditText edtTitle, edtDescription;
    private Spinner spnType;
    private Button btnSubmit;
    private ImageButton btnBack;
    private TextView tvRoomInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        bindViews();
        setupRoomInfo();
        setupSpinner();
        setupActions();
    }

    private void bindViews() {
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        spnType = findViewById(R.id.spnType);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        tvRoomInfo = findViewById(R.id.tvRoomInfo);
    }

    private void setupRoomInfo() {
        String room = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        String house = getIntent().getStringExtra(EXTRA_HOUSE_NAME);
        if (room != null && house != null) {
            tvRoomInfo.setText(room + " · " + house);
        }
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.feedback_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Mục 0 là gợi ý -> tô màu xám nhạt
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(position == 0 ? 0xFF5F6F8F : 0xFF1F2B4D);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        int typePos = spnType.getSelectedItemPosition();

        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Vui lòng nhập tiêu đề");
            edtTitle.requestFocus();
            return;
        }
        if (typePos == 0) {
            Toast.makeText(this, "Vui lòng chọn loại vấn đề", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            edtDescription.setError("Vui lòng mô tả sự cố");
            edtDescription.requestFocus();
            return;
        }

        String type = spnType.getSelectedItem().toString();
        btnSubmit.setEnabled(false);

        // TODO: gửi lên server / Firebase tại đây với (title, type, desc)
        Toast.makeText(this, "Đã gửi phản ánh: " + title + " (" + type + ")", Toast.LENGTH_SHORT).show();
        finish();
    }
}