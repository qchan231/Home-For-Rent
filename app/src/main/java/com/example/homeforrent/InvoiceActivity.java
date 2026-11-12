package com.example.homeforrent;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_NAME = "room_name";
    public static final String EXTRA_HOUSE_NAME = "house_name";

    private final List<Invoice> allInvoices = new ArrayList<>();
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private InvoiceAdapter adapter;
    private TextView tvDate, tvEmpty;
    private EditText edtSearch;
    private Spinner spnStatus;
    private RecyclerView rv;

    /** Ngày dùng để tính "trễ hạn". */
    private long refMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invoice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setupRoomInfo();

        // TODO: khi dùng dữ liệu thật, đổi thành System.currentTimeMillis()
        refMillis = date(2025, 3, 9);

        loadData();
        setupList();
        setupFilters();
        applyFilters();
    }

    private void bindViews() {
        tvDate = findViewById(R.id.tvDate);
        tvEmpty = findViewById(R.id.tvEmpty);
        edtSearch = findViewById(R.id.edtSearch);
        spnStatus = findViewById(R.id.spnStatus);
        rv = findViewById(R.id.rvInvoices);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRoomInfo() {
        String room = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        String house = getIntent().getStringExtra(EXTRA_HOUSE_NAME);
        if (room != null && house != null) {
            ((TextView) findViewById(R.id.tvRoomInfo)).setText(room + " · " + house);
        }
    }

    /** TODO: thay bằng dữ liệu thật từ server / Firebase / SQLite. */
    private void loadData() {
        allInvoices.clear();
        allInvoices.add(new Invoice(1, 2025, date(2025, 2, 7), 1_450_000, true));
        allInvoices.add(new Invoice(2, 2025, date(2025, 3, 7), 1_674_750, false));
        allInvoices.add(new Invoice(3, 2025, date(2025, 4, 7), 1_450_000, false));
    }

    private void setupList() {
        adapter = new InvoiceAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    private void setupFilters() {
        tvDate.setText(dateFmt.format(new Date(refMillis)));

        findViewById(R.id.btnDate).setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(refMillis);
            new DatePickerDialog(this, (view, y, m, d) -> {
                refMillis = date(y, m + 1, d);
                tvDate.setText(dateFmt.format(new Date(refMillis)));
                applyFilters();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        ArrayAdapter<CharSequence> sa = ArrayAdapter.createFromResource(
                this, R.array.invoice_filters, android.R.layout.simple_spinner_item);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(sa);
        spnStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (v instanceof TextView) ((TextView) v).setTextColor(0xFF1F2B4D);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) { }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { applyFilters(); }
        });
    }

    /** Lọc theo từ khóa + trạng thái, rồi nhóm: Trễ hạn -> Chưa đóng -> Đã đóng. */
    private void applyFilters() {
        String q = edtSearch.getText().toString().trim().toLowerCase(Locale.getDefault());
        int filter = spnStatus.getSelectedItemPosition(); // 0 tất cả, 1 trễ hạn, 2 chưa đóng, 3 đã đóng

        List<Invoice> late = new ArrayList<>(), unpaid = new ArrayList<>(), paid = new ArrayList<>();
        for (Invoice inv : allInvoices) {
            String text = ("tháng " + inv.month + "/" + inv.year).toLowerCase(Locale.getDefault());
            if (!q.isEmpty() && !text.contains(q)) continue;

            switch (inv.statusAt(refMillis)) {
                case LATE:   if (filter == 0 || filter == 1) late.add(inv); break;
                case UNPAID: if (filter == 0 || filter == 2) unpaid.add(inv); break;
                default:     if (filter == 0 || filter == 3) paid.add(inv); break;
            }
        }

        List<InvoiceAdapter.Row> rows = new ArrayList<>();
        addGroup(rows, "Trễ hạn", Invoice.Status.LATE, late);
        addGroup(rows, "Chưa đóng", Invoice.Status.UNPAID, unpaid);
        addGroup(rows, "Đã đóng", Invoice.Status.PAID, paid);

        adapter.setRows(rows);
        boolean empty = rows.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void addGroup(List<InvoiceAdapter.Row> out, String title,
                          Invoice.Status status, List<Invoice> list) {
        if (list.isEmpty()) return;
        // Mới nhất lên trước
        Collections.sort(list, (a, b) -> (b.year * 12 + b.month) - (a.year * 12 + a.month));
        out.add(InvoiceAdapter.Row.header(status, title, list.size()));
        for (Invoice inv : list) {
            out.add(InvoiceAdapter.Row.item(status, inv, inv.daysLateAt(refMillis)));
        }
    }

    private static long date(int y, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(y, m - 1, d);
        return c.getTimeInMillis();
    }
}