package com.dotee313.omatydom.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dotee313.omatydom.core.model.Citizen;

import java.util.ArrayList;
import java.util.List;

/**
 * Arabic citizen card built entirely in code, so it never conflicts with the
 * shared XML layout. The game keeps ticking while the dialog is open.
 *
 * <p>The card is a snapshot of real core values assembled by MainActivity.
 * Fields the model does not store are explicitly marked as unrecorded.</p>
 */
public final class CitizenInfoDialog {

    public interface OnAppointListener {
        void onAppoint(Citizen citizen);
    }

    /** Icon kinds, all resolved through the new PixelIcons.cardIcon badges. */
    public static final int ICON_KINGDOM = 0;
    public static final int ICON_CLAN = 1;
    public static final int ICON_CULTURE = 2;
    public static final int ICON_FAMILY = 3;
    public static final int ICON_SKILL = 4;
    public static final int ICON_STATUS = 5;
    public static final int ICON_LANGUAGE = 6;
    public static final int ICON_SPECIALTY = 7;

    /** One labelled fact: the label is dim and bold, the value is bright. */
    public static final class Row {
        public final String label;
        public final String value;

        public Row(String label, String value) {
            this.label = label == null ? "" : label;
            this.value = value == null ? "" : value;
        }
    }

    /** A titled, icon-led group of facts with its own accent colour. */
    public static final class Section {
        public final String title;
        public final int icon;
        public final int color;
        public final List<Row> rows;

        public Section(String title, int icon, int color, List<Row> rows) {
            this.title = title == null ? "" : title;
            this.icon = icon;
            this.color = color;
            this.rows = rows == null ? new ArrayList<Row>() : rows;
        }
    }

    /** Ordered sections shown inside the scrollable body of the card. */
    public static final class Card {
        public final List<Section> sections;

        public Card(List<Section> sections) {
            this.sections = sections == null ? new ArrayList<Section>() : sections;
        }
    }

    private CitizenInfoDialog() {
    }

    /** Portrait header plus scrollable, divided sections of truthful facts. */
    public static AlertDialog show(Activity activity, Citizen citizen, String subtitle,
                                   Card card, OnAppointListener listener) {
        float density = activity.getResources().getDisplayMetrics().density;
        int pad = Math.round(16f * density);
        int portraitSize = Math.round(72f * density);

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
        name.setTextSize(19f);
        name.setTypeface(null, Typeface.BOLD);
        identity.addView(name);
        TextView subtitleView = new TextView(activity);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(0xFFB0BEC5);
        subtitleView.setTextSize(12f);
        subtitleView.setLineSpacing(1f, 1.12f);
        identity.addView(subtitleView);
        header.addView(identity,
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(header);

        View headerRule = new View(activity);
        headerRule.setBackgroundColor(0xFFFFCA28);
        root.addView(headerRule, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, dp(1.5f, density))));

        LinearLayout bodyWrap = new LinearLayout(activity);
        bodyWrap.setOrientation(LinearLayout.VERTICAL);
        bodyWrap.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        bodyWrap.setPadding(0, pad / 3, 0, 0);

        if (card != null) {
            List<Section> sections = card.sections;
            for (int i = 0; i < sections.size(); i++) {
                Section section = sections.get(i);
                bodyWrap.addView(sectionHeading(activity, section, density));
                for (Row row : section.rows) {
                    bodyWrap.addView(rowView(activity, row, section, density));
                }
                if (i < sections.size() - 1) {
                    View divider = new View(activity);
                    divider.setBackgroundColor(0x33FFFFFF);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            Math.max(1, Math.round(density)));
                    params.topMargin = Math.round(7f * density);
                    params.bottomMargin = Math.round(2f * density);
                    divider.setLayoutParams(params);
                    bodyWrap.addView(divider);
                }
            }
        }

        ScrollView scroll = new ScrollView(activity);
        int maxHeight = Math.min(Math.round(360f * density),
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

    private static View sectionHeading(Activity activity, Section section, float density) {
        LinearLayout heading = new LinearLayout(activity);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        heading.setPadding(0, dp(4f, density), 0, dp(3f, density));

        ImageView icon = new ImageView(activity);
        icon.setImageDrawable(iconFor(activity, section.icon));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setBackgroundResource(R.drawable.bg_panel);
        heading.addView(icon, new LinearLayout.LayoutParams(dp(18f, density), dp(18f, density)));

        TextView title = new TextView(activity);
        title.setText(section.title);
        title.setTextColor(0xFFFFCA28);
        title.setTextSize(13f);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(dp(6f, density), 0, 0, 0);
        heading.addView(title, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return heading;
    }

    private static View rowView(Activity activity, Row row, Section section, float density) {
        LinearLayout line = new LinearLayout(activity);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.TOP);
        line.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        line.setPadding(dp(3f, density), dp(2f, density), 0, dp(2f, density));

        // Section accent stays as a slim bar; the badge itself is the new card art.
        View accent = new View(activity);
        accent.setBackgroundColor(section.color);
        line.addView(accent, new LinearLayout.LayoutParams(
                dp(2f, density), dp(14f, density)));

        PixelIcons.CardSection badge = cardSectionForLabel(row.label);
        if (badge == null) {
            badge = cardSectionForKind(section.icon);
        }
        ImageView icon = new ImageView(activity);
        icon.setImageDrawable(PixelIcons.cardIcon(activity, badge));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setContentDescription(section.title + " — " + row.label);
        line.addView(icon, new LinearLayout.LayoutParams(
                dp(18f, density), dp(18f, density)));

        String label = row.label.isEmpty() ? "" : row.label + "  ";
        SpannableString text = new SpannableString(label + row.value);
        int labelEnd = label.length();
        if (labelEnd > 0) {
            text.setSpan(new ForegroundColorSpan(0xFFB0BEC5), 0, labelEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, labelEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        text.setSpan(new ForegroundColorSpan(0xFFFFFFFF), labelEnd, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView value = new TextView(activity);
        value.setText(text);
        value.setTextSize(11.5f);
        value.setLineSpacing(0f, 1.12f);
        value.setPadding(dp(5f, density), 0, 0, 0);
        line.addView(value, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return line;
    }

    private static Drawable iconFor(Activity activity, int kind) {
        return PixelIcons.cardIcon(activity, cardSectionForKind(kind));
    }

    /** Maps the public icon kinds to the painter's new card badges. */
    private static PixelIcons.CardSection cardSectionForKind(int kind) {
        switch (kind) {
            case ICON_CLAN:
                return PixelIcons.CardSection.CLAN;
            case ICON_CULTURE:
                return PixelIcons.CardSection.CULTURE;
            case ICON_FAMILY:
                return PixelIcons.CardSection.PARTNER;
            case ICON_SKILL:
                return PixelIcons.CardSection.SKILLS;
            case ICON_STATUS:
                return PixelIcons.CardSection.STATUS;
            case ICON_LANGUAGE:
                return PixelIcons.CardSection.LANGUAGE;
            case ICON_SPECIALTY:
                return PixelIcons.CardSection.SPECIALTY;
            case ICON_KINGDOM:
            default:
                return PixelIcons.CardSection.KINGDOM;
        }
    }

    /** Row-level badges make language, specialty and traits visible too. */
    private static PixelIcons.CardSection cardSectionForLabel(String label) {
        if (label == null) {
            return null;
        }
        switch (label) {
            case "المملكة":
            case "الحاكم":
                return PixelIcons.CardSection.KINGDOM;
            case "العشيرة":
            case "شيخ العشيرة":
            case "أفراد العشيرة":
                return PixelIcons.CardSection.CLAN;
            case "الديانة":
                return PixelIcons.CardSection.FAITH;
            case "اللغة":
                return PixelIcons.CardSection.LANGUAGE;
            case "الزوج/الشريك":
                return PixelIcons.CardSection.PARTNER;
            case "القرابة":
                return PixelIcons.CardSection.CLAN;
            case "الحمل":
                return PixelIcons.CardSection.PREGNANCY;
            case "المهارة":
            case "المشغول":
                return PixelIcons.CardSection.SKILLS;
            case "العرق":
            case "الشعب":
            case "الفكرة":
                return PixelIcons.CardSection.TRAITS;
            case "التخصص":
                return PixelIcons.CardSection.SPECIALTY;
            default:
                return null;
        }
    }

    private static int dp(float value, float density) {
        return Math.max(1, Math.round(value * density));
    }
}
