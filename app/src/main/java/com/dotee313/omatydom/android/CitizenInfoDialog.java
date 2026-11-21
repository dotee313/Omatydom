package com.dotee313.omatydom.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dotee313.omatydom.core.model.Citizen;

/**
 * Arabic citizen card built entirely in code, so it never conflicts with the
 * shared XML layout. The game keeps ticking while the dialog is open.
 */
public final class CitizenInfoDialog {

    public interface OnAppointListener {
        void onAppoint(Citizen citizen);
    }

    private CitizenInfoDialog() {
    }

    /** Shows the existing portrait beside the identity and a scrollable fact list. */
    public static AlertDialog show(Activity activity, Citizen citizen, String subtitle,
                                   String details, OnAppointListener listener) {
        float density = activity.getResources().getDisplayMetrics().density;
        int pad = Math.round(16f * density);
        int portraitSize = Math.round(64f * density);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setPadding(pad, pad, pad, pad / 2);

        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        ImageView portrait = new ImageView(activity);
        portrait.setImageDrawable(PixelIcons.portrait(activity, citizen));
        portrait.setScaleType(ImageView.ScaleType.FIT_CENTER);
        portrait.setAdjustViewBounds(false);
        portrait.setBackgroundResource(R.drawable.bg_panel);
        portrait.setContentDescription("صورة " + citizen.getName());
        header.addView(portrait, new LinearLayout.LayoutParams(portraitSize, portraitSize));

        LinearLayout identity = new LinearLayout(activity);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView name = new TextView(activity);
        name.setText(citizen.getName());
        name.setTextColor(0xFFFFF3E0);
        name.setTextSize(18f);
        name.setTypeface(null, Typeface.BOLD);
        identity.addView(name);
        TextView subtitleView = new TextView(activity);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(0xFFB0BEC5);
        subtitleView.setTextSize(12f);
        identity.addView(subtitleView);
        header.addView(identity,
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(header);

        TextView body = new TextView(activity);
        body.setText(details);
        body.setTextColor(0xFFFFFFFF);
        body.setTextSize(12f);
        body.setLineSpacing(2f, 1.12f);

        LinearLayout bodyWrap = new LinearLayout(activity);
        bodyWrap.setOrientation(LinearLayout.VERTICAL);
        bodyWrap.setPadding(0, pad / 2, 0, 0);
        bodyWrap.addView(body, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(activity);
        int maxHeight = Math.min(Math.round(320f * density),
                activity.getResources().getDisplayMetrics().heightPixels / 2);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, maxHeight));
        scroll.addView(bodyWrap, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("بطاقة السكان")
                .setView(root)
                .setPositiveButton("تعيين حاكم", (d, which) -> {
                    d.dismiss();
                    if (listener != null) {
                        listener.onAppoint(citizen);
                    }
                })
                .setNegativeButton("إغلاق", null)
                .create();
        dialog.show();
        return dialog;
    }
}
