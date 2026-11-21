package com.dotee313.omatydom.android;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Small accessors for the original artwork supplied by the user.
 *
 * <p>Only plain framework APIs are used: {@link Context#getDrawable(int)} is
 * available from API 21, so no AndroidX dependency is needed.</p>
 */
public final class UserArt {

    private UserArt() {
        // Utility class.
    }

    public static Drawable logo(Context context) {
        return load(context, R.drawable.ic_user_logo);
    }

    public static Drawable info(Context context) {
        return load(context, R.drawable.ic_user_info);
    }

    public static Drawable settings(Context context) {
        return load(context, R.drawable.ic_user_settings);
    }

    public static Drawable mods(Context context) {
        return load(context, R.drawable.ic_user_mods);
    }

    private static Drawable load(Context context, int resId) {
        Drawable drawable = context.getDrawable(resId);
        return drawable == null ? null : drawable.mutate();
    }

    /**
     * Resolves one of the {@code user_*} colours from {@code user_palette.xml}.
     * Resources.getColor(int, Theme) and Context.getColor(int) require API 23;
     * the deprecated overload is intentional here because minSdk is 21.
     */
    @SuppressWarnings("deprecation")
    public static int color(Context context, int resId) {
        return context.getResources().getColor(resId);
    }

    /** Short Arabic credit text describing the standalone game. */
    public static String aboutText() {
        return "أوماتيدوم لعبة مستقلة تعمل دون اتصال بالإنترنت، وتدور حول عالم مصغّر حيّ. "
                + "الشعار والأيقونات وخلفية القائمة من تصميمك الأصلي، وقد تم تبسيطها لتناسب واجهة اللعبة. "
                + "لا تستخدم اللعبة أي أصول أو نصوص من ألعاب أخرى.";
    }
}
