package com.example.foundit;

import android.content.Context;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class PopupDialogHelper {

    public static void show(Context context, View contentView) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(contentView);
        dialog.setCancelable(true);
        dialog.show();
    }
}
