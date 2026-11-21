package com.dotee313.omatydom.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Job;

/** Top-bar pixel controls plus detailed little portraits. */
final class PixelIcons {

    enum Control { PAUSE, SPEED, CONTROL, ASSASSINATE, NEXT }

    private static final String[] PAUSE = {
            ".........",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            "........."
    };

    private static final String[] SPEED = {
            ".........",
            "K....K...",
            "KK...KK..",
            "KKK..KKK.",
            "KK...KK..",
            "K....K...",
            ".........",
            ".........",
            "........."
    };

    private static final String[] CROWN = {
            ".........",
            "Y...Y...Y",
            "YY..Y..YY",
            "YYYYYYYYY",
            "YWYYYYYWY",
            "YYYYYYYYY",
            ".YYYYYYY.",
            ".YYYYYYY.",
            "........."
    };

    private static final String[] DAGGER = {
            ".........",
            ".......K.",
            "......KW.",
            ".....KW..",
            "....KW...",
            "...KW....",
            "..KK.....",
            ".K.......",
            "........."
    };

    private static final String[] NEXT = {
            ".........",
            "..B......",
            "..BB.....",
            "..BBB....",
            "..BBBB...",
            "..BBBBB..",
            "..BBBB...",
            "..BBB....",
            "........."
    };

    private PixelIcons() {
        // utility class
    }

    static Drawable control(Context context, Control control) {
        switch (control) {
            case SPEED:
                return pattern(context, SPEED);
            case CONTROL:
                return pattern(context, CROWN);
            case ASSASSINATE:
                return pattern(context, DAGGER);
            case NEXT:
                return pattern(context, NEXT);
            case PAUSE:
            default:
                return pattern(context, PAUSE);
        }
    }

    /** Decree icons come from the supplied WorldBox diplomacy art pack. */
    static int decreeImage(Decree decree) {
        switch (decree) {
            case FESTIVAL:
                return R.drawable.decree_festival;
            case TAX_CUT:
                return R.drawable.decree_tax_cut;
            case TAX_RAISE:
                return R.drawable.decree_tax_raise;
            case BUILD_TEMPLE:
                return R.drawable.decree_temple;
            case AMNESTY:
                return R.drawable.decree_amnesty;
            case CONSCRIPTION:
                return R.drawable.decree_conscription;
            case FREE_TRADE:
                return R.drawable.decree_free_trade;
            case CRACKDOWN:
                return R.drawable.decree_crackdown;
            default:
                return R.drawable.decree_festival;
        }
    }

    /** A 12×12 shaded bust: skin tone, hair, eyes, clothing and crown. */
    static Drawable portrait(Context context, Citizen citizen) {
        int size = 12;
        int[] pixels = new int[size * size];
        int skin = mix(citizen.getRace().getColor(), 0xFFD8A47C, 0.48f);
        int skinDark = shade(skin, 0.72f);
        int skinLight = mix(skin, 0xFFFFFFFF, 0.22f);
        int hair = citizen.getAgeYears() > 55 ? 0xFF9E9E9E
                : (citizen.getGender() == Gender.FEMALE ? 0xFF5D4037 : 0xFF3E2723);
        int hairLight = mix(hair, 0xFFFFFFFF, 0.18f);
        int cloth = citizen.getJob().getColor();
        int clothDark = shade(cloth, 0.68f);
        int eye = 0xFF202124;

        fill(pixels, 0x00000000);

        // Hair mass and ears.
        rect(pixels, size, 2, 1, 9, 2, hair);
        rect(pixels, size, 3, 0, 8, 0, hairLight);
        rect(pixels, size, 1, 4, 1, 6, skinDark);
        rect(pixels, size, 10, 4, 10, 6, skinDark);

        // Face with a light side and a shaded side.
        rect(pixels, size, 2, 3, 9, 7, skin);
        rect(pixels, size, 2, 3, 3, 7, skinDark);
        rect(pixels, size, 8, 3, 9, 7, skinDark);
        rect(pixels, size, 4, 4, 7, 6, skinLight);

        // Eyes, nose and mouth.
        set(pixels, size, 4, 4, eye);
        set(pixels, size, 7, 4, eye);
        set(pixels, size, 4, 5, 0xFFFFFFFF);
        set(pixels, size, 7, 5, 0xFFFFFFFF);
        set(pixels, size, 6, 5, skinDark);
        rect(pixels, size, 5, 6, 6, 6, shade(skinDark, 0.8f));

        // Longer hair for women.
        if (citizen.getGender() == Gender.FEMALE) {
            rect(pixels, size, 1, 3, 1, 8, hair);
            rect(pixels, size, 10, 3, 10, 8, hair);
        }

        // Neck and shoulders.
        rect(pixels, size, 5, 8, 6, 8, skinDark);
        rect(pixels, size, 2, 9, 9, 11, cloth);
        rect(pixels, size, 2, 9, 3, 11, clothDark);
        rect(pixels, size, 8, 9, 9, 11, clothDark);
        rect(pixels, size, 5, 10, 6, 10, shade(cloth, 1.25f));

        if (citizen.getJob() == Job.RULER) {
            rect(pixels, size, 2, 0, 9, 0, 0xFFFFD54F);
            rect(pixels, size, 4, 1, 4, 1, 0xFFFFD54F);
            rect(pixels, size, 7, 1, 7, 1, 0xFFFFD54F);
        }
        if (citizen.isPregnant()) {
            rect(pixels, size, 4, 10, 7, 11, mix(cloth, 0xFFE57373, 0.35f));
        }
        return fromPixels(context, size, size, pixels);
    }

    private static Drawable pattern(Context context, String[] rows) {
        int width = rows[0].length();
        int height = rows.length;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            if (rows[y].length() != width) {
                throw new IllegalStateException("Bad pixel icon row: " + rows[y]);
            }
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = colorOf(rows[y].charAt(x));
            }
        }
        return fromPixels(context, width, height, pixels);
    }

    private static int colorOf(char symbol) {
        switch (symbol) {
            case 'K': return 0xFF202124;
            case 'W': return 0xFFFFFFFF;
            case 'Y': return 0xFFFFD54F;
            case 'G': return 0xFF9E9E9E;
            case 'B': return 0xFF42A5F5;
            case 'P': return 0xFFAB47BC;
            case 'N': return 0xFF8D6E63;
            default: return Color.TRANSPARENT;
        }
    }

    private static void fill(int[] pixels, int color) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = color;
        }
    }

    private static void rect(int[] pixels, int width, int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                set(pixels, width, x, y, color);
            }
        }
    }

    private static void set(int[] pixels, int width, int x, int y, int color) {
        if (x >= 0 && y >= 0 && x < width && y * width + x < pixels.length) {
            pixels[y * width + x] = color;
        }
    }

    private static int shade(int color, float factor) {
        int r = Math.min(255, Math.round(Color.red(color) * factor));
        int g = Math.min(255, Math.round(Color.green(color) * factor));
        int b = Math.min(255, Math.round(Color.blue(color) * factor));
        return Color.argb(Color.alpha(color), r, g, b);
    }

    private static int mix(int a, int b, float weightOfB) {
        float w = Math.max(0f, Math.min(1f, weightOfB));
        int r = Math.round(Color.red(a) * (1 - w) + Color.red(b) * w);
        int g = Math.round(Color.green(a) * (1 - w) + Color.green(b) * w);
        int bl = Math.round(Color.blue(a) * (1 - w) + Color.blue(b) * w);
        return Color.argb(255, r, g, bl);
    }

    private static Drawable fromPixels(Context context, int width, int height, int[] pixels) {
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.getPaint().setAntiAlias(false);
        drawable.getPaint().setFilterBitmap(false);
        return drawable;
    }
}
