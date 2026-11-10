package com.example.homeforrent;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogHelper {

    public static void show(Context context, int iconRes, String title, String message,
                            String buttonText, Runnable onButtonClick) {
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

        dialog.show();
    }
}