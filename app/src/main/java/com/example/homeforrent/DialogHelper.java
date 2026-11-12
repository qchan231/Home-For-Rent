package com.example.homeforrent;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogHelper {

    // Hàm cũ: giữ nguyên cách gọi, không có nút Hủy
    public static void show(Context context, int iconRes, String title, String message,
                            String buttonText, Runnable onButtonClick) {
        show(context, iconRes, title, message, buttonText, null, onButtonClick);
    }

    // Hàm mới: có nút Hủy khi cancelText != null
    public static void show(Context context, int iconRes, String title, String message,
                            String buttonText, String cancelText, Runnable onButtonClick) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_message);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ((ImageView) dialog.findViewById(R.id.imgIcon)).setImageResource(iconRes);
        ((TextView) dialog.findViewById(R.id.tvTitle)).setText(title);
        ((TextView) dialog.findViewById(R.id.tvMessage)).setText(message);

        Button btn = dialog.findViewById(R.id.btnAction);
        btn.setText(buttonText);
        btn.setOnClickListener(v -> {
            dialog.dismiss();
            if (onButtonClick != null) onButtonClick.run();
        });

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        if (cancelText != null) {
            btnCancel.setText(cancelText);
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}