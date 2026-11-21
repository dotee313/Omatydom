package com.dotee313.omatydom.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.Power;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Job;

/** Code-only pixel art: top controls, tabs, overlays and small system badges. */
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

    private static final String[] POWER_RAIN = {
            "...WWW...", "..WWWWW..", ".WWWWWWW.", "WWWWWWWWW", ".........",
            "..B.B.B..", ".B.B.B.B.", "..B.B.B..", "........."
    };
    private static final String[] POWER_BLOOM = {
            "....G....", "...GGG...", "..GGGGG..", ".GGYGGGG.", "GGGYYGGG.",
            ".GGYGGGG.", "..GGGGG..", "...GGG...", "....G...."
    };
    private static final String[] POWER_HEAL = {
            "....W....", "...WWW...", "....W....", ".WWWWWWW.", "..WWWWW..",
            ".WWWWWWW.", "....W....", "...WWW...", "....W...."
    };
    private static final String[] POWER_BLESS = {
            "....Y....", "..Y.Y.Y..", "...YYY...", ".YYYYYYY.", "..YYYYY..",
            "...YYY...", "..Y.Y.Y..", ".Y..Y..Y.", "........."
    };
    private static final String[] POWER_LIGHTNING = {
            "......Y..", ".....YY..", "....YY...", "...YYY...", "..YYY....",
            "...YY....", "..YY.....", ".YY......", "Y........"
    };
    private static final String[] POWER_PLAGUE = {
            "..KKKKK..", ".KKKKKKK.", "KKWKKKWKK", "KKWKKKWKK", "KKKKKKKKK",
            ".KWWWWWK.", "KKKKKKKKK", ".K.K.K.K.", "..K...K.."
    };
    private static final String[] POWER_EARTHQUAKE = {
            "K...K...K", ".K.K.K.K.", "..KKKKK..", "KKKKKKKKK", "..K.K.K..",
            ".K.K.K.K.", "K...K...K", "..K...K..", ".K.....K."
    };
    private static final String[] POWER_METEOR = {
            "........Y", ".......YY", "......YY.", "....YYYY.", "...YYYYY.",
            "..YYYYYY.", ".YYYYYYY.", "YYYYYYYY.", "........."
    };
    private static final String[] POWER_PEACE = {
            "..WW...WW", ".WWWW.WWW", "WWWWWWWWW", ".WWWWWWW.", "..WWWWW..",
            "...WWW...", "....W....", "...WWW...", "..W...W.."
    };
    private static final String[] POWER_PROSPERITY = {
            "..YYYYY..", ".YYYWYYY.", "YYYYYYYYY", "YYYWYYYYY", "YYYYYYYYY",
            ".YYYWYYY.", "..YYYYY..", "...YYY...", "....Y...."
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

    // Seven distinct bottom-tab badges.
    private static final String[] NAV_WORLD = {
            ".........", "..GGGGG..", ".GGLLLGG.", "GLLYYYLGG", "GLLYYYLGG",
            ".GLLLLLG.", "..BBBBB..", ".B.B.B.B.", "........."
    };
    private static final String[] NAV_PEOPLE = {
            ".........", "..KK.KK..", ".KKK.KKK.", "..KK.KK..", "...KKK...",
            "..WWWWW..", ".WWWWWWW.", "WWWWWWWWW", "........."
    };
    private static final String[] NAV_NATURE = {
            ".........", "......LL.", "....LLLL.", "...LLLL..", "..LLLLL..",
            ".LLLKK...", "LLK......", "K........", "........."
    };
    private static final String[] NAV_CREATURES = {
            "..R...R..", "..RR.RR..", "RRRRRRRRR", ".RRRRRRR.", "..RWKWR..",
            "..RRRRR..", "...R.R...", "..R...R..", "........."
    };
    private static final String[] NAV_KINGDOMS = {
            "Y...Y...Y", "YY..Y..YY", "YYYYYYYYY", "YWYYYYYWY", "YYYYYYYYY",
            ".NNNNNNN.", ".N.N.N.N.", ".NNNNNNN.", "........."
    };
    private static final String[] NAV_POWERS = {
            "....Y....", "...YYY...", "..YYYYY..", ".YYYYYYY.", "YYYPYYYYY",
            ".YYYYYYY.", "..YYYYY..", "...YYY...", "....Y...."
    };
    private static final String[] NAV_EVENTS = {
            ".........", ".WWWWWWW.", ".WKKKKKW.", ".WWWWWWW.", ".WKKKKKW.",
            ".WWWWWWW.", ".WKKKKKW.", ".WWWWWWW.", "........."
    };

    // Five map-overlay badges.
    private static final String[] OVER_TERRAIN = {
            ".........", "G..G..G..", "GG.GG.GG.", "GGGGGGGGG", ".GGGGGGG.",
            "..GGGGG..", "...GGG...", "....G....", "........."
    };
    private static final String[] OVER_BIOME = {
            ".........", "..LL.LL..", ".LLLLLLL.", "LLLLLLLLL", "LLLLLLLLL",
            ".LLLLLLL.", "..L...L..", ".L.....L.", "........."
    };
    private static final String[] OVER_KINGDOM = {
            ".Y.......", ".YY......", ".YYY.....", ".YYYY....", ".YYYYY...",
            ".NYYYYY..", ".NNYYYY..", ".NNNYY...", ".NNNN...."
    };
    private static final String[] OVER_RELIGION = {
            "....Y....", "..Y.Y.Y..", "...YYY...", ".YYYYYYY.", "..YYYYY..",
            "...YYY...", "..Y.Y.Y..", ".Y..Y..Y.", "........."
    };
    private static final String[] OVER_JOB = {
            ".........", ".....NN..", "....NNN..", "...NNN...", "..NNN....",
            ".KKK.....", "KK.......", "K........", "........."
    };

    private static final String[] LAW_ON = {
            ".........", "...GGG...", "..GGGGG..", ".GGWWWGG.", "GGGGGGGGG",
            "..W...W..", ".WW...WW.", "WW.....WW", "........."
    };
    private static final String[] LAW_OFF = {
            ".........", "...KKK...", "..KKKKK..", ".KKRRRKK.", "KKKKKKKKK",
            "..R...R..", ".RR...RR.", "RR.....RR", "........."
    };

    private static final String[] CLAN = {
            ".........", "..Y...Y..", "..NN.NN..", "NNNNNNNNN", ".NNNNNNN.",
            "..NNNNN..", "..N.N.N..", ".NN...NN.", "........."
    };

    // Decree badges, generated rather than loaded from an art pack.
    private static final String[] DECREE_FESTIVAL = {
            "...YYY...", "..YYYYY..", ".YYYYYYY.", "YYYWYYYYY", "YYYYYYYYY",
            "..YWWWY..", ".WWY.WWW.", "..W...W..", "........."
    };
    private static final String[] DECREE_TAX_CUT = {
            "..YYYYY..", ".YYYYYYY.", "YYYYYYYYY", "YYYYY....", "YYYY.....",
            "YYY......", "YY.......", "Y........", "........."
    };
    private static final String[] DECREE_TAX_RAISE = {
            "Y........", "YY.......", "YYY......", "YYYY.....", "YYYYY....",
            "YYYYYYYYY", ".YYYYYYY.", "..YYYYY..", "........."
    };
    private static final String[] DECREE_TEMPLE = {
            "..YYYYY..", ".YYYYYYY.", "YYYYYYYYY", "..YNYNY..", "..YYYYY..",
            "..YYYYY..", "..NNNNN..", ".NNNNNNN.", "........."
    };
    private static final String[] DECREE_AMNESTY = {
            ".........", "....WWW..", "..WWWWWW.", ".WWWWWWWW", "WWWWWWWWW",
            ".WWWWWWW.", "..WWWWW..", "...WWW...", "........."
    };
    private static final String[] DECREE_CONSCRIPTION = {
            ".......K.", "......KK.", ".....KK..", "....KK...", "...KK....",
            "..KK.....", ".KK......", "K........", "........."
    };
    private static final String[] DECREE_FREE_TRADE = {
            ".....W...", "....WW...", "WWWWWWWW.", ".NNNNNNN.", "..NNNNN..",
            "...NNN...", "..BBBBB..", ".B.B.B.B.", "........."
    };
    private static final String[] DECREE_CRACKDOWN = {
            "..KKKKK..", ".KKKKKKK.", "KKKKKKKKK", "KKKWWWKKK", "KKKWWWKKK",
            ".KKWWWKK.", "..KKKKK..", "...KKK...", "....K...."
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

    /** One generated badge per bottom-navigation tab. */
    static Drawable nav(Context context, int index) {
        switch (index) {
            case 1: return pattern(context, NAV_PEOPLE);
            case 2: return pattern(context, NAV_NATURE);
            case 3: return pattern(context, NAV_CREATURES);
            case 4: return pattern(context, NAV_KINGDOMS);
            case 5: return pattern(context, NAV_POWERS);
            case 6: return pattern(context, NAV_EVENTS);
            case 0:
            default: return pattern(context, NAV_WORLD);
        }
    }

    /** Badges for the five map overlays. */
    static Drawable overlay(Context context, int index) {
        switch (index) {
            case 1: return pattern(context, OVER_BIOME);
            case 2: return pattern(context, OVER_KINGDOM);
            case 3: return pattern(context, OVER_RELIGION);
            case 4: return pattern(context, OVER_JOB);
            case 0:
            default: return pattern(context, OVER_TERRAIN);
        }
    }

    static Drawable lawIcon(Context context, boolean enabled) {
        return pattern(context, enabled ? LAW_ON : LAW_OFF);
    }

    static Drawable biomeSwatch(Context context, int color) {
        int[] pixels = new int[81];
        fill(pixels, Color.TRANSPARENT);
        rect(pixels, 9, 0, 0, 8, 8, shade(color, 0.55f));
        rect(pixels, 9, 1, 1, 7, 7, color);
        rect(pixels, 9, 2, 2, 4, 3, mix(color, 0xFFFFFFFF, 0.38f));
        rect(pixels, 9, 6, 5, 7, 6, shade(color, 0.72f));
        return fromPixels(context, 9, 9, pixels);
    }

    static Drawable resourceSwatch(Context context, int color) {
        int[] pixels = new int[81];
        fill(pixels, Color.TRANSPARENT);
        int light = mix(color, 0xFFFFFFFF, 0.42f);
        int dark = shade(color, 0.62f);
        rect(pixels, 9, 3, 0, 5, 1, light);
        rect(pixels, 9, 1, 2, 7, 3, color);
        rect(pixels, 9, 0, 4, 8, 5, color);
        rect(pixels, 9, 2, 6, 6, 7, dark);
        rect(pixels, 9, 3, 8, 5, 8, dark);
        return fromPixels(context, 9, 9, pixels);
    }

    static Drawable disasterIcon(Context context, int color) {
        int[] pixels = new int[81];
        fill(pixels, Color.TRANSPARENT);
        int light = mix(color, 0xFFFFFFFF, 0.5f);
        rect(pixels, 9, 4, 0, 4, 2, color);
        rect(pixels, 9, 4, 6, 4, 8, color);
        rect(pixels, 9, 0, 4, 2, 4, color);
        rect(pixels, 9, 6, 4, 8, 4, color);
        rect(pixels, 9, 2, 2, 6, 6, color);
        rect(pixels, 9, 3, 3, 5, 5, light);
        rect(pixels, 9, 1, 1, 1, 1, light);
        rect(pixels, 9, 7, 7, 7, 7, light);
        return fromPixels(context, 9, 9, pixels);
    }

    static Drawable techIcon(Context context, int color) {
        int[] pixels = new int[81];
        fill(pixels, Color.TRANSPARENT);
        int dark = shade(color, 0.65f);
        int light = mix(color, 0xFFFFFFFF, 0.45f);
        rect(pixels, 9, 2, 2, 6, 6, dark);
        rect(pixels, 9, 3, 1, 5, 1, color);
        rect(pixels, 9, 3, 7, 5, 7, color);
        rect(pixels, 9, 1, 3, 1, 5, color);
        rect(pixels, 9, 7, 3, 7, 5, color);
        rect(pixels, 9, 3, 3, 5, 5, light);
        return fromPixels(context, 9, 9, pixels);
    }

    static Drawable clanIcon(Context context) {
        return pattern(context, CLAN);
    }

    static Drawable kingdomIcon(Context context) {
        return pattern(context, CROWN);
    }

    static Drawable power(Context context, Power power) {
        switch (power) {
            case RAIN: return pattern(context, POWER_RAIN);
            case BLOOM: return pattern(context, POWER_BLOOM);
            case HEAL: return pattern(context, POWER_HEAL);
            case BLESS: return pattern(context, POWER_BLESS);
            case LIGHTNING: return pattern(context, POWER_LIGHTNING);
            case PLAGUE: return pattern(context, POWER_PLAGUE);
            case EARTHQUAKE: return pattern(context, POWER_EARTHQUAKE);
            case METEOR: return pattern(context, POWER_METEOR);
            case PEACE: return pattern(context, POWER_PEACE);
            case PROSPERITY:
            default: return pattern(context, POWER_PROSPERITY);
        }
    }

    /** Decree art is now generated in code too — no bundled art pack is used. */
    static Drawable decreeImage(Context context, Decree decree) {
        switch (decree) {
            case FESTIVAL: return pattern(context, DECREE_FESTIVAL);
            case TAX_CUT: return pattern(context, DECREE_TAX_CUT);
            case TAX_RAISE: return pattern(context, DECREE_TAX_RAISE);
            case BUILD_TEMPLE: return pattern(context, DECREE_TEMPLE);
            case AMNESTY: return pattern(context, DECREE_AMNESTY);
            case CONSCRIPTION: return pattern(context, DECREE_CONSCRIPTION);
            case FREE_TRADE: return pattern(context, DECREE_FREE_TRADE);
            case CRACKDOWN: return pattern(context, DECREE_CRACKDOWN);
            default: return pattern(context, DECREE_FESTIVAL);
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
            case 'R': return 0xFFEF5350;
            case 'L': return 0xFF66BB6A;
            case 'C': return 0xFF26C6DA;
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
