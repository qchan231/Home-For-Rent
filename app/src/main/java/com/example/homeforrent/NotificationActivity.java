package com.example.homeforrent;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationActivity extends AppCompatActivity {

    private static final int TAB_GENERAL = 0;
    private static final int TAB_INVOICE = 1;

    private TextView tabGeneral, tabInvoice, tvEmptyMessage;
    private View layoutEmpty;
    private RecyclerView rvNotifications;
    private EditText edtSearch;
    private int currentTab = TAB_GENERAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tabGeneral = findViewById(R.id.tabGeneral);
        tabInvoice = findViewById(R.id.tabInvoice);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        rvNotifications = findViewById(R.id.rvNotifications);
        edtSearch = findViewById(R.id.edtSearch);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tabGeneral.setOnClickListener(v -> selectTab(TAB_GENERAL));
        tabInvoice.setOnClickListener(v -> selectTab(TAB_INVOICE));

        selectTab(TAB_GENERAL);
    }

    private void selectTab(int tab) {
        currentTab = tab;
        boolean isGeneral = tab == TAB_GENERAL;

        // Tab đang chọn: nền gradient xanh + chữ trắng. Tab còn lại: không nền + chữ xám.
        tabGeneral.setBackgroundResource(isGeneral ? R.drawable.bg_tab_selected : 0);
        tabGeneral.setTextColor(Color.parseColor(isGeneral ? "#FFFFFF" : "#52607E"));
        tabInvoice.setBackgroundResource(isGeneral ? 0 : R.drawable.bg_tab_selected);
        tabInvoice.setTextColor(Color.parseColor(isGeneral ? "#52607E" : "#FFFFFF"));

        loadNotifications();
    }

    private void loadNotifications() {
        // TODO: gọi API lấy thông báo theo currentTab (chung / hóa đơn),
        // lọc theo edtSearch rồi gán vào rvNotifications.
        // Tạm thời chưa có dữ liệu nên hiện trạng thái trống.
        boolean hasData = false;

        layoutEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        rvNotifications.setVisibility(hasData ? View.VISIBLE : View.GONE);

        tvEmptyMessage.setText(currentTab == TAB_GENERAL
                ? "Hiện tại bạn chưa nhận được thông báo chung nào."
                : "Hiện tại bạn chưa nhận được thông báo hóa đơn nào.");
    }
}