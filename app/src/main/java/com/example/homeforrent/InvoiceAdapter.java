package com.example.homeforrent;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** 1 dòng trong danh sách: tiêu đề nhóm hoặc 1 hóa đơn. */
    public static class Row {
        final boolean header;
        final Invoice.Status status;
        final String title;
        final int count;
        final Invoice invoice;
        final long daysLate;

        private Row(boolean header, Invoice.Status status, String title, int count,
                    Invoice invoice, long daysLate) {
            this.header = header;
            this.status = status;
            this.title = title;
            this.count = count;
            this.invoice = invoice;
            this.daysLate = daysLate;
        }

        public static Row header(Invoice.Status s, String title, int count) {
            return new Row(true, s, title, count, null, 0);
        }

        public static Row item(Invoice.Status s, Invoice inv, long daysLate) {
            return new Row(false, s, null, 0, inv, daysLate);
        }
    }

    private static final int TYPE_HEADER = 0, TYPE_ITEM = 1;

    private final List<Row> rows = new ArrayList<>();
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private final DecimalFormat moneyFmt;

    public InvoiceAdapter() {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        sym.setGroupingSeparator('.');
        moneyFmt = new DecimalFormat("#,###", sym);
    }

    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).header ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(inf.inflate(R.layout.item_invoice_header, parent, false));
        }
        return new ItemVH(inf.inflate(R.layout.item_invoice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row r = rows.get(position);
        int fg = fgColor(r.status), bg = bgColor(r.status);

        if (holder instanceof HeaderVH) {
            HeaderVH h = (HeaderVH) holder;
            h.title.setText(r.title);
            h.count.setText(String.valueOf(r.count));
            h.count.setTextColor(fg);
            h.count.setBackgroundTintList(ColorStateList.valueOf(bg));
        } else {
            ItemVH h = (ItemVH) holder;
            Invoice inv = r.invoice;
            h.month.setText("Tháng " + inv.month + "/" + inv.year);
            h.due.setText("Hạn thanh toán: " + dateFmt.format(new Date(inv.dueMillis)));
            h.amount.setText(moneyFmt.format(inv.amount) + " đồng");

            h.badge.setText(badgeText(r));
            h.badge.setTextColor(fg);
            h.badge.setBackgroundTintList(ColorStateList.valueOf(bg));

            h.icon.setBackgroundTintList(ColorStateList.valueOf(bg));
            h.icon.setColorFilter(fg);
        }
    }

    @Override
    public int getItemCount() { return rows.size(); }

    private String badgeText(Row r) {
        switch (r.status) {
            case LATE: return "Trễ hạn (" + r.daysLate + " ngày)";
            case PAID: return "Đã thanh toán";
            default: return "Chưa đóng";
        }
    }

    public static int fgColor(Invoice.Status s) {
        switch (s) {
            case LATE: return Color.parseColor("#B42318");
            case PAID: return Color.parseColor("#17703A");
            default: return Color.parseColor("#1A55C7");
        }
    }

    public static int bgColor(Invoice.Status s) {
        switch (s) {
            case LATE: return Color.parseColor("#FDECEC");
            case PAID: return Color.parseColor("#E6F6EC");
            default: return Color.parseColor("#E3EEFF");
        }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView title, count;
        HeaderVH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvGroupTitle);
            count = v.findViewById(R.id.tvGroupCount);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView month, due, badge, amount;
        ItemVH(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.ivIcon);
            month = v.findViewById(R.id.tvMonth);
            due = v.findViewById(R.id.tvDue);
            badge = v.findViewById(R.id.tvBadge);
            amount = v.findViewById(R.id.tvAmount);
        }
    }
}