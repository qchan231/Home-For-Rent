package com.example.homeforrent;

import java.util.Calendar;

public class Invoice {

    public enum Status { LATE, UNPAID, PAID }

    public final int month, year;
    public final long dueMillis;   // hạn thanh toán
    public final long amount;      // VNĐ
    public final boolean paid;

    public Invoice(int month, int year, long dueMillis, long amount, boolean paid) {
        this.month = month;
        this.year = year;
        this.dueMillis = dueMillis;
        this.amount = amount;
        this.paid = paid;
    }

    /** Trạng thái tại thời điểm nowMillis: đã đóng -> PAID; quá hạn -> LATE; còn lại -> UNPAID. */
    public Status statusAt(long nowMillis) {
        if (paid) return Status.PAID;
        return startOfDay(nowMillis) > startOfDay(dueMillis) ? Status.LATE : Status.UNPAID;
    }

    public long daysLateAt(long nowMillis) {
        long diff = startOfDay(nowMillis) - startOfDay(dueMillis);
        return Math.max(0, Math.round(diff / 86_400_000.0));
    }

    public static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}