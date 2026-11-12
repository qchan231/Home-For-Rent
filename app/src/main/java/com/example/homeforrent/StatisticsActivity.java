package com.example.homeforrent;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_HOUSE_NAME = "house_name";

    /** Dữ liệu 1 tháng. */
    private static class MonthData {
        final int year, month;
        final float kwh, water;

        MonthData(int year, int month, float kwh, float water) {
            this.year = year;
            this.month = month;
            this.kwh = kwh;
            this.water = water;
        }

        int key() { return year * 12 + (month - 1); }
    }

    /** Một thẻ thống kê (điện hoặc nước). */
    private class StatCard {
        final View root;
        final TextView tvFrom, tvTo, tvHighMonth, tvHighValue, tvLowMonth, tvLowValue;
        final BarChart chart;
        final boolean isElectric;
        final String unit;
        final int colorHigh, colorNormal;

        int fromKey, toKey;

        StatCard(View root, boolean isElectric) {
            this.root = root;
            this.isElectric = isElectric;
            this.unit = isElectric ? "kWh" : "m³";
            this.colorHigh = Color.parseColor(isElectric ? "#F5A300" : "#1E7BFF");
            this.colorNormal = Color.parseColor(isElectric ? "#FFD98A" : "#9CC4FF");

            ((TextView) root.findViewById(R.id.tvCardTitle))
                    .setText(isElectric ? "Thống kê tiền điện" : "Thống kê tiền nước");
            ((TextView) root.findViewById(R.id.tvCardSub))
                    .setText(isElectric ? "Lượng điện tiêu thụ mỗi tháng" : "Lượng nước tiêu thụ mỗi tháng");
            ((TextView) root.findViewById(R.id.tvUnit)).setText("(" + unit + ")");
            // TODO: đổi sang ic_electric / ic_water nếu bạn có
            ((ImageView) root.findViewById(R.id.ivCardIcon)).setImageResource(R.drawable.ic_statistics);

            tvFrom = root.findViewById(R.id.tvFromValue);
            tvTo = root.findViewById(R.id.tvToValue);
            tvHighMonth = root.findViewById(R.id.tvHighMonth);
            tvHighValue = root.findViewById(R.id.tvHighValue);
            tvLowMonth = root.findViewById(R.id.tvLowMonth);
            tvLowValue = root.findViewById(R.id.tvLowValue);
            chart = root.findViewById(R.id.chart);

            setupChart();

            // Mặc định: toàn bộ khoảng dữ liệu đang có
            fromKey = allData.get(0).key();
            toKey = allData.get(allData.size() - 1).key();

            root.findViewById(R.id.btnFrom).setOnClickListener(v ->
                    showMonthPicker(fromKey, key -> {
                        if (key > toKey) {
                            toast("Tháng bắt đầu phải nhỏ hơn hoặc bằng tháng kết thúc");
                            return;
                        }
                        fromKey = key;
                        refresh();
                    }));
            root.findViewById(R.id.btnTo).setOnClickListener(v ->
                    showMonthPicker(toKey, key -> {
                        if (key < fromKey) {
                            toast("Tháng kết thúc phải lớn hơn hoặc bằng tháng bắt đầu");
                            return;
                        }
                        toKey = key;
                        refresh();
                    }));
            refresh();
        }

        void setupChart() {
            chart.getDescription().setEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.getAxisRight().setEnabled(false);
            chart.setScaleEnabled(false);
            chart.setDoubleTapToZoomEnabled(false);
            chart.setExtraBottomOffset(4f);
            chart.setNoDataText("Chưa có dữ liệu");

            XAxis x = chart.getXAxis();
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setGranularity(1f);
            x.setDrawGridLines(false);
            x.setTextColor(Color.parseColor("#5F6F8F"));
            x.setTextSize(11f);

            chart.getAxisLeft().setAxisMinimum(0f);
            chart.getAxisLeft().setTextColor(Color.parseColor("#5F6F8F"));
            chart.getAxisLeft().setGridColor(Color.parseColor("#D5E1FB"));
            chart.getAxisLeft().enableGridDashedLine(8f, 6f, 0f);
            chart.getAxisLeft().setDrawAxisLine(false);
        }

        /** Lọc dữ liệu theo khoảng tháng, cập nhật text + biểu đồ. */
        void refresh() {
            tvFrom.setText(formatKey(fromKey, true));
            tvTo.setText(formatKey(toKey, true));

            List<MonthData> list = new ArrayList<>();
            for (MonthData d : allData) {
                if (d.key() >= fromKey && d.key() <= toKey) list.add(d);
            }

            if (list.isEmpty()) {
                chart.clear();
                tvHighMonth.setText("--");
                tvHighValue.setText("--");
                tvLowMonth.setText("--");
                tvLowValue.setText("--");
                return;
            }

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            MonthData max = list.get(0), min = list.get(0);
            for (int i = 0; i < list.size(); i++) {
                MonthData d = list.get(i);
                float v = valueOf(d);
                entries.add(new BarEntry(i, v));
                labels.add(String.format(Locale.US, "%02d/%02d", d.month, d.year % 100));
                if (v > valueOf(max)) max = d;
                if (v < valueOf(min)) min = d;
            }

            tvHighMonth.setText("Tháng " + formatKey(max.key(), true));
            tvHighValue.setText(formatValue(valueOf(max)));
            tvLowMonth.setText("Tháng " + formatKey(min.key(), true));
            tvLowValue.setText(formatValue(valueOf(min)));

            // Cột cao nhất tô đậm, còn lại nhạt
            List<Integer> colors = new ArrayList<>();
            for (BarEntry e : entries) {
                colors.add(e.getY() == valueOf(max) ? colorHigh : colorNormal);
            }

            BarDataSet set = new BarDataSet(entries, "");
            set.setColors(colors);
            set.setValueTextSize(11f);
            set.setValueTextColor(Color.parseColor("#1F2B4D"));

            BarData data = new BarData(set);
            data.setBarWidth(0.6f);

            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            chart.getXAxis().setLabelCount(labels.size());
            chart.setData(data);
            chart.animateY(400);
            chart.invalidate();
        }

        float valueOf(MonthData d) { return isElectric ? d.kwh : d.water; }

        String formatValue(float v) {
            return isElectric
                    ? String.format(Locale.US, "%.0f %s", v, unit)
                    : String.format(Locale.US, "%.1f %s", v, unit);
        }
    }

    private interface OnMonthPicked { void onPicked(int key); }

    private final List<MonthData> allData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String room = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        String house = getIntent().getStringExtra(EXTRA_HOUSE_NAME);
        if (room != null && house != null) {
            ((TextView) findViewById(R.id.tvRoomInfo)).setText(room + " · " + house);
        }

        loadData();
        new StatCard(findViewById(R.id.cardElectric), true);
        new StatCard(findViewById(R.id.cardWater), false);
    }

    /** TODO: thay bằng dữ liệu thật từ server / Firebase / SQLite. */
    private void loadData() {
        allData.clear();
        allData.add(new MonthData(2024, 11, 190, 6.3f));
        allData.add(new MonthData(2024, 12, 340, 11.4f));
        allData.add(new MonthData(2025, 1, 150, 5f));
        allData.add(new MonthData(2025, 2, 450, 15f));
        allData.add(new MonthData(2025, 3, 235, 7.8f));
    }

    /** Dialog chọn tháng/năm bằng 2 NumberPicker (không cần thư viện ngoài). */
    private void showMonthPicker(int currentKey, OnMonthPicked callback) {
        NumberPicker npMonth = new NumberPicker(this);
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setValue(currentKey % 12 + 1);

        NumberPicker npYear = new NumberPicker(this);
        npYear.setMinValue(2020);
        npYear.setMaxValue(2040);
        npYear.setValue(currentKey / 12);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(android.view.Gravity.CENTER);
        box.setPadding(0, 24, 0, 0);
        box.addView(npMonth);
        box.addView(npYear);

        new AlertDialog.Builder(this)
                .setTitle("Chọn tháng")
                .setView(box)
                .setPositiveButton("Đồng ý", (d, w) ->
                        callback.onPicked(npYear.getValue() * 12 + (npMonth.getValue() - 1)))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private static String formatKey(int key, boolean withYear4) {
        int year = key / 12;
        int month = key % 12 + 1;
        return String.format(Locale.US, "%02d/%d", month, year);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}