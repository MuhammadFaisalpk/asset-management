package com.app.assetmaintenance.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.app.assetmaintenance.R;

public class LoadingDialog {

    private Context context;
    private TextView tv_title;
    private Dialog dialog;

    public LoadingDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.getWindow().requestFeature((Window.FEATURE_NO_TITLE));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progress_dailog);
        tv_title = dialog.findViewById(R.id.tv_title);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }


    public void SetTitle(String title) {
        tv_title.setText(title);
    }

    public void Cancelable(boolean type) {
        dialog.setCancelable(true);
    }

    public void Show() {
        dialog.show();
    }

    public void Dismiss() {
        dialog.dismiss();
    }

    public void SetFullWidth() {
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }
}