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

/** Tiny hand-made pixel icons. No image assets, no anti-aliasing, almost no memory. */
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

    private static final String[] FESTIVAL = {
            "....K....",
            "...YKY...",
            "..YYYYY..",
            "..YWWWY..",
            "..YWWWY..",
            "..YYYYY..",
            "...YYY...",
            "....Y....",
            "........."
    };

    private static final String[] TAX_CUT = {
            ".........",
            "..YYYYY..",
            ".YWWWWWY.",
            ".YWKKKWY.",
            ".YWKKKWY.",
            ".YWWWWWY.",
            "..YYYYY..",
            ".........",
            "........."
    };

    private static final String[] TAX_RAISE = {
            ".........",
            "..YYYYY..",
            ".YWWWWWY.",
            ".YWKWKWY.",
            ".YWKKKWY.",
            ".YWKWKWY.",
            ".YWWWWWY.",
            "..YYYYY..",
            "........."
    };

    private static final String[] TEMPLE = {
            "....Y....",
            "...YYY...",
            "..YYYYY..",
            ".WWWWWWW.",
            ".WKWWWKW.",
            ".WWWWWWW.",
            ".NNNNNNN.",
            ".NNNNNNN.",
            "........."
    };

    private static final String[] AMNESTY = {
            ".........",
            ".PP...PP.",
            "PPPPP.PPP",
            "PPPPPPPPP",
            ".PPPPPPP.",
            "..PPPPP..",
            "...PPP...",
            "....P....",
            "........."
    };

    private static final String[] CONSCRIPTION = {
            ".........",
            "..GGGGG..",
            ".GGWWWGG.",
            ".GGWWWGG.",
            "..GGGGG..",
            "...GGG...",
            "....K....",
            "...KKK...",
            "........."
    };

    private static final String[] FREE_TRADE = {
            ".........",
            "....N....",
            "...NNN...",
            "..NNNNN..",
            ".NNNNNNN.",
            "NNNNNNNNN",
            "..BBBBB..",
            "...BBB...",
            "........."
    };

    private static final String[] CRACKDOWN = {
            "...GGG...",
            "..GKKKG..",
            ".GKKKKKG.",
            ".GKWWWKG.",
            ".GKKKKKG.",
            "..GKKKG..",
            "...GGG...",
            "...GGG...",
            "........."
    };

    private PixelIcons() {
        // utility class
    }

    static Drawable control(Context context, Control control) {
        switch (control) {
            case PAUSE:
                return pattern(context, PAUSE);
            case SPEED:
                return pattern(context, SPEED);
            case CONTROL:
                return pattern(context, CROWN);
            case ASSASSINATE:
                return pattern(context, DAGGER);
            case NEXT:
                return pattern(context, NEXT);
            default:
                return pattern(context, PAUSE);
        }
    }

    static Drawable decree(Context context, Decree decree) {
        switch (decree) {
            case FESTIVAL:
                return pattern(context, FESTIVAL);
            case TAX_CUT:
                return pattern(context, TAX_CUT);
            case TAX_RAISE:
                return pattern(context, TAX_RAISE);
            case BUILD_TEMPLE:
                return pattern(context, TEMPLE);
            case AMNESTY:
                return pattern(context, AMNESTY);
            case CONSCRIPTION:
                return pattern(context, CONSCRIPTION);
            case FREE_TRADE:
                return pattern(context, FREE_TRADE);
            case CRACKDOWN:
                return pattern(context, CRACKDOWN);
            default:
                return pattern(context, FESTIVAL);
        }
    }

    /** A 9×9 face built from race, gender and job colours. */
    static Drawable portrait(Context context, Citizen citizen) {
        int size = 9;
        int[] pixels = new int[size * size];
        int dark = 0xFF212121;
        int hair = citizen.getGender() == Gender.FEMALE ? 0xFF6D4C41 : 0xFF3E2723;
        int skin = citizen.getRace().getColor();
        int cloth = citizen.getJob().getColor();

        rect(pixels, size, 2, 2, 6, 5, skin);
        rect(pixels, size, 1, 1, 7, 2, hair);
        rect(pixels, size, 2, 3, 2, 4, hair);
        rect(pixels, size, 6, 3, 6, 4, hair);
        if (citizen.getGender() == Gender.FEMALE) {
            rect(pixels, size, 1, 2, 1, 5, hair);
            rect(pixels, size, 7, 2, 7, 5, hair);
        }
        rect(pixels, size, 3, 4, 3, 4, dark);
        rect(pixels, size, 5, 4, 5, 4, dark);
        rect(pixels, size, 2, 6, 6, 7, cloth);
        rect(pixels, size, 3, 8, 3, 8, dark);
        rect(pixels, size, 5, 8, 5, 8, dark);
        if (citizen.getJob() == Job.RULER) {
            rect(pixels, size, 2, 0, 6, 0, 0xFFFFD54F);
            rect(pixels, size, 4, 1, 4, 1, 0xFFFFD54F);
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
            case 'R': return 0xFFE53935;
            case 'B': return 0xFF42A5F5;
            case 'O': return 0xFFFB8C00;
            case 'P': return 0xFFAB47BC;
            case 'N': return 0xFF8D6E63;
            case 'L': return 0xFF66BB6A;
            default: return Color.TRANSPARENT;
        }
    }

    private static void rect(int[] pixels, int width, int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                if (x >= 0 && y >= 0 && x < width && y * width + x < pixels.length) {
                    pixels[y * width + x] = color;
                }
            }
        }
    }

    private static Drawable fromPixels(Context context, int width, int height, int[] pixels) {
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.getPaint().setAntiAlias(false);
        drawable.getPaint().setFilterBitmap(false);
        return drawable;
    }
}
